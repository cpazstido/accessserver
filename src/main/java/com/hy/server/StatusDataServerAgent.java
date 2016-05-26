package com.hy.server;

import com.hy.decoder.NettyMessageDecoder;
import com.hy.encoder.NettyMessageEncoder;
import com.hy.handler.FireServerHandler;
import com.hy.handler.StatusDataServerHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.log4j.Logger;

/**
 * Created by cpazstido on 2016/5/26.
 */
public class StatusDataServerAgent extends ServerAgent{
    private static Logger logger=Logger.getLogger(StatusDataServerAgent.class);
    public StatusDataServerAgent(String serverName, int port){
        this.setServerName(serverName);
        this.setPort(port);
    }

    public void bind(){
        // 配置服务端的NIO线程组
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .option(ChannelOption.SO_BROADCAST, true)
                    .handler(new StatusDataServerHandler());
            b.option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(2048));
            logger.debug(getServerName()+" starts successfully！begin to listen at:" + getPort());
            b.bind(getPort()).sync().channel().closeFuture().await();
            //b.bind(getPort()).sync();
        } catch (Exception e){
            logger.error(e);
        }finally {
            group.shutdownGracefully();
        }
    }
}
