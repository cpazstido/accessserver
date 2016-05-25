package com.hy.server;

import com.hy.decoder.NettyMessageDecoder;
import com.hy.encoder.MyNettyEncoder;
import com.hy.encoder.NettyMessageEncoder;
import com.hy.handler.FireServerHandler;
import com.hy.handler.WebServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.apache.log4j.Logger;

/**
 * Created by cpazstido on 2016/5/23.
 */
public class FireServerAgent extends ServerAgent{
    private static Logger logger=Logger.getLogger(FireServerAgent.class);
    public FireServerAgent(String serverName, int port){
        this.setServerName(serverName);
        this.setPort(port);
    }

    public void bind(){
        // 配置服务端的NIO线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch)
                                throws Exception {
                            ch.pipeline().addLast("decoder", new NettyMessageDecoder());
                            ch.pipeline().addLast("encoder", new NettyMessageEncoder());
                            ch.pipeline().addLast("fireServerHandler", new FireServerHandler());
                        }
                    });
            // 绑定端口，同步等待成功
            ChannelFuture f = b.bind(getPort()).sync();
            logger.debug(getServerName()+" starts successfully！begin to listen at:" + getPort());

            // 等待服务端监听端口关闭
            f.channel().closeFuture().sync();
        }catch (Exception e){
            logger.error(e);
        }finally {
            // 优雅退出，释放线程池资源
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
