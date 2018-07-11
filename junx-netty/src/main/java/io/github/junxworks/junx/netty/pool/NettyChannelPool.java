/*
 ***************************************************************************************
 * 
 * @Title:  NettyChannelPool.java   
 * @Package io.github.junxworks.junx.netty.pool   
 * @Description: (用一句话描述该文件做什么)   
 * @author: Michael
 * @date:   2018-7-11 15:52:42   
 * @version V1.0 
 * @Copyright: 2018 JunxWorks. All rights reserved. 
 * 
 *  ---------------------------------------------------------------------------------- 
 * 文件修改记录
 *     文件版本：         修改人：             修改原因：
 ***************************************************************************************
 */
package io.github.junxworks.junx.netty.pool;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.junxworks.junx.core.exception.FatalException;
import io.github.junxworks.junx.core.lifecycle.Service;
import io.github.junxworks.junx.core.util.SystemUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelHealthChecker;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.pool.FixedChannelPool.AcquireTimeoutAction;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

/**
 * 为netty提供长连接的连接池
 *
 * @ClassName:  NettyConnPool
 * @author: Michael
 * @date:   2018-5-29 18:02:07
 * @since:  v1.0
 */
public class NettyChannelPool extends Service {

	private EventLoopGroup eventLoopGroup;

	private static final Logger logger = LoggerFactory.getLogger(NettyChannelPool.class);

	/** fixpool. */
	private FixedChannelPool fixpool = null;

	private int maxConnect = SystemUtils.SYS_PROCESSORS;

	private int connTimeout = 2000;

	private SocketAddress serverAddress;

	private ChannelPoolHandler poolHandler;

	/** 心跳检测. */
	private ChannelHealthChecker healthCheck = ChannelHealthChecker.ACTIVE;

	/** 请求连接超时时候的动作. */
	private AcquireTimeoutAction acquireTimeoutAction;

	private long acquireTimeoutMillis = -1;

	/** 最大等待请求连接数. */
	private int maxPendingAcquires = Integer.MAX_VALUE;

	/** 归还连接的时候是否检测健康状况. */
	private boolean releaseHealthCheck = true;

	/** 获取连接的策略，true的话就是LIFO，否则FIFO. */
	private boolean lastRecentUsed = false;

	private ChannelInitializer<Channel> channelInitializer;

	public NettyChannelPool(SocketAddress serverAddress, ChannelInitializer<Channel> channelInitializer) {
		this.serverAddress = serverAddress;
		this.channelInitializer = channelInitializer;
	}

	/**
	 * 构造一个新的 netty conn pool 对象.
	 *
	 * @param serverAddress the server address
	 * @param handler the handler
	 */
	public NettyChannelPool(InetSocketAddress serverAddress, ChannelPoolHandler poolHandler) {
		this.serverAddress = serverAddress;
		this.poolHandler = poolHandler;
	}

	public EventLoopGroup getEventLoopGroup() {
		return eventLoopGroup;
	}

	public void setEventLoopGroup(EventLoopGroup eventLoopGroup) {
		this.eventLoopGroup = eventLoopGroup;
	}

	public int getConnTimeout() {
		return connTimeout;
	}

	public void setConnTimeout(int connTimeout) {
		this.connTimeout = connTimeout;
	}

	public int getMaxConnect() {
		return maxConnect;
	}

	public void setMaxConnect(int maxConnect) {
		this.maxConnect = maxConnect;
	}

	public SocketAddress getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(SocketAddress serverAddress) {
		this.serverAddress = serverAddress;
	}

	public ChannelPoolHandler getPoolHandler() {
		return poolHandler;
	}

	public void setPoolHandler(ChannelPoolHandler poolHandler) {
		this.poolHandler = poolHandler;
	}

	public ChannelHealthChecker getHealthCheck() {
		return healthCheck;
	}

	public void setHealthCheck(ChannelHealthChecker healthCheck) {
		this.healthCheck = healthCheck;
	}

	public AcquireTimeoutAction getAcquireTimeoutAction() {
		return acquireTimeoutAction;
	}

	public void setAcquireTimeoutAction(AcquireTimeoutAction acquireTimeoutAction) {
		this.acquireTimeoutAction = acquireTimeoutAction;
	}

	public long getAcquireTimeoutMillis() {
		return acquireTimeoutMillis;
	}

	public void setAcquireTimeoutMillis(long acquireTimeoutMillis) {
		this.acquireTimeoutMillis = acquireTimeoutMillis;
	}

	public int getMaxPendingAcquires() {
		return maxPendingAcquires;
	}

	public void setMaxPendingAcquires(int maxPendingAcquires) {
		this.maxPendingAcquires = maxPendingAcquires;
	}

	public boolean isReleaseHealthCheck() {
		return releaseHealthCheck;
	}

	public void setReleaseHealthCheck(boolean releaseHealthCheck) {
		this.releaseHealthCheck = releaseHealthCheck;
	}

	public boolean isLastRecentUsed() {
		return lastRecentUsed;
	}

	public void setLastRecentUsed(boolean lastRecentUsed) {
		this.lastRecentUsed = lastRecentUsed;
	}

	public ChannelInitializer<Channel> getChannelInitializer() {
		return channelInitializer;
	}

	public void setChannelInitializer(ChannelInitializer<Channel> channelInitializer) {
		this.channelInitializer = channelInitializer;
	}

	/**
	 * Acquire.
	 *
	 * @param timeout the timeout
	 * @return the channel
	 */
	//申请连接，没有申请到(或者网络断开)，返回null
	public Channel acquire(int timeoutMillis) throws Exception {
		try {
			Future<Channel> fch = fixpool.acquire();
			Channel ch = fch.get(timeoutMillis, TimeUnit.MILLISECONDS);
			return ch;
		} catch (Exception e) {
			logger.error("Exception accurred when acquire channel from channel pool.", e);
			throw e;
		}
	}

	/**
	 * Release.
	 *
	 * @param channel the channel
	 */
	//释放连接
	public void release(Channel channel) {
		try {
			if (channel != null) {
				fixpool.release(channel);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void doStart() throws Throwable {
		ChannelPoolHandler handler = null;
		if (poolHandler != null) {
			handler = poolHandler;
		} else {
			if (channelInitializer == null) {
				throw new FatalException("Parameter \"poolHandler\" and \"channelInitializer\" can not both be null.");
			}
			handler = new DefaultPoolHandler(channelInitializer);
		}
		if (eventLoopGroup == null) {
			eventLoopGroup = new NioEventLoopGroup();
		}
		Bootstrap b = new Bootstrap();
		b.group(eventLoopGroup);
		Class<SocketChannel> socketChanellClass = null;
		if(eventLoopGroup instanceof EpollEventLoopGroup) {
			socketChanellClass = (Class<SocketChannel>) Class.forName(EpollSocketChannel.class.getCanonicalName());
		}else {
			socketChanellClass = (Class<SocketChannel>) Class.forName(NioSocketChannel.class.getCanonicalName());
		}
		b.channel(socketChanellClass);
		b.option(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark.DEFAULT); //通道水位线，超过这个水位对应channel的isWritable就会变成false，用于做流量控制
		b.option(ChannelOption.TCP_NODELAY, true);
		b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connTimeout);
		b.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
		b.option(ChannelOption.SO_RCVBUF, 1024 * 32);
		b.option(ChannelOption.SO_SNDBUF, 1024 * 32);
		//		b.option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT);
		b.remoteAddress(serverAddress);
		fixpool = new FixedChannelPool(b, handler, healthCheck, acquireTimeoutAction, acquireTimeoutMillis, maxConnect, maxPendingAcquires, releaseHealthCheck, lastRecentUsed);
	}

	@Override
	protected void doStop() throws Throwable {
		if (fixpool != null) {
			fixpool.close();
			fixpool = null;
		}
	}
}
