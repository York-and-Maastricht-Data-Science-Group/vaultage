package org.vaultage.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/***
 * The Netty implementation of direct message client. TODO The class should be
 * extending class Thread so that it doesn't block other processes and runs in
 * background.
 * 
 * @author Alfa Yohannis
 *
 */
public class NettyDirectMessageClient implements DirectMessageClient {

	String serverName;
	int port;
	private InetSocketAddress address;
	private ChannelFuture channelFuture;
	private EventLoopGroup workerGroup;
	private Bootstrap bootstrap;

	/***
	 * For testing or dummy of this Netty direct message client.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws InterruptedException {
		try {
			DirectMessageClient client = new NettyDirectMessageClient(Vaultage.DEFAULT_SERVER_ADDRESS,
					Vaultage.DEFAULT_SERVER_PORT);
//			DirectMessageClient client = new NettyDirectMessageClient("192.168.56.101", 9998);
			client.connect();
			client.sendMessage(UUID.randomUUID().toString());
			client.disconnect();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 * The constructor of the direct message client with the direct message server's
	 * address parameter.
	 * 
	 * @param serverAddress the ip/hostname and port of the direct message server
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public NettyDirectMessageClient(InetSocketAddress serverAddress) throws UnknownHostException, IOException {
		this(serverAddress.getAddress().getHostAddress(), serverAddress.getPort());
	}

	/***
	 * The constructor of the direct message client with the direct message server's
	 * op/hostname and port parameters.
	 * 
	 * @param serverName the ip/hostname of the direct message server
	 * @param port       the port of the direct message server
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	public NettyDirectMessageClient(String serverName, int port) throws UnknownHostException, IOException {
		this.serverName = serverName;
		this.port = port;
		this.address = new InetSocketAddress(serverName, port);
	}

	/***
	 * To check if the client has successfully connected to the direct message
	 * server.
	 * 
	 * @return true if the connection is active, false if it's inactive
	 */
	public boolean isActive() {
		if (this.channelFuture == null || this.channelFuture.channel() == null) {
			return false;
		} else {
			return this.channelFuture.channel().isActive();
		}
	}

	/***
	 * To connect to a direct message server using its ip/hostname and port.
	 * 
	 * @param serverName the direct message server's ip or hostname
	 * @param port       the direct message server's port
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean connect(String serverName, int port) throws IOException, InterruptedException {
		this.serverName = serverName;
		this.port = port;
		this.address = new InetSocketAddress(serverName, port);
		return this.connect();
	}

	/***
	 * To connect to a direct message server using its ip/hostname and port if the
	 * ip/hostname and port have been already defined.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public boolean connect() throws IOException, InterruptedException {
		workerGroup = new NioEventLoopGroup();

		try {
			bootstrap = new Bootstrap();
			bootstrap.group(workerGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				public void initChannel(SocketChannel ch) throws Exception {
					/**
					 * TODO This line based frame decoder can be a problem when the number of
					 * characters in a message is longer than Integer.MAX_VALUE. A custom line-based
					 * frame decoder shoud be defined.
					 **/
					ch.pipeline().addLast(new LineBasedFrameDecoder(Integer.MAX_VALUE));
					ch.pipeline().addLast(new StringDecoder());
					ch.pipeline().addLast(new StringEncoder());
					ch.pipeline().addLast(new NettyClientHandler());
				}
			});

			channelFuture = bootstrap.connect(address).sync();

//			 send message
//			channelFuture.channel().writeAndFlush(System.lineSeparator());

			// Wait until the connection is closed.
//			channelFuture.channel().closeFuture().sync();

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
//		finally {
//			workerGroup.shutdownGracefully();
//		}
		return false;
	}

	/***
	 * To re-connect to a direct message server using its ip/hostname and port.
	 * 
	 * @param serverName the direct message server's ip or hostname
	 * @param port       the direct message server's port
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void reconnect(String serverName, int port) throws IOException {

	}

	/***
	 * To re-connect to a direct message server with the ip/hostname and port if the
	 * ip/hostname and port have been already defined.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void reconnect() throws IOException {

	}

	/***
	 * To disconnect from a direct message server.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void disconnect() throws IOException, InterruptedException {
		channelFuture.channel().closeFuture();
	}

	/***
	 * To send a message to the direct message server
	 * 
	 * @param message the message
	 * @throws IOException
	 */
	public void sendMessage(String message) throws IOException {
		channelFuture.channel().writeAndFlush(message + System.lineSeparator());
	}

	/***
	 * To check if the client has successfully connected to the direct message
	 * server.
	 * 
	 * @return true if the connection is active, false if it's inactive
	 */
	@Override
	public void shutdown() throws IOException, InterruptedException {
		this.disconnect();
		workerGroup.shutdownGracefully();
	}

	/***
	 * A handler when the direct message client receives a message from the server.
	 * 
	 * @author Alfa Yohannis
	 *
	 */
	public class NettyClientHandler extends ChannelInboundHandlerAdapter {

		/***
		 * The method that is called when an active connection to the direct message
		 * server has just been established.
		 */
		@Override
		public void channelActive(ChannelHandlerContext ctx) throws Exception {
//			ctx.writeAndFlush(UUID.randomUUID().toString());
		}

		/***
		 * The method to handle the message received from a server is defined here.
		 */
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object msg) {
//			ByteBuf in = (ByteBuf) msg;
//			try {
//				while (in.isReadable()) {
//					System.out.print((char) in.readByte());
//					System.out.flush();
//				}
//			} finally {
//				ReferenceCountUtil.release(msg);
//			}
		}

		/***
		 * The method to define a response when an exception has been caught during
		 * receiving a message.
		 */
		@Override
		public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
			cause.printStackTrace();
			ctx.close();
		}
	}

	/***
	 * To get the address of the direct message server
	 * 
	 * @return the ip/hostname and port
	 */
	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) this.channelFuture.channel().remoteAddress();
	}

	/***
	 * To get the local address of the client
	 * 
	 * @return the ip/hostname and port
	 */
	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) this.channelFuture.channel().localAddress();
	}
}
