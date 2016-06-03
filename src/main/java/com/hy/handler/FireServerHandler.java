package com.hy.handler;

import com.hy.bean.MessageTypeResp;
import com.hy.bean.NettyMessage;
import com.hy.resolver.FireDataResolver;
import com.hy.utils.PropertyUtils;
import com.hy.utils.RandomCode;
import com.hy.utils.RedisUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import redis.clients.jedis.Jedis;

import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Created by cpazstido on 2016/5/10.
 */
public class FireServerHandler extends ChannelHandlerAdapter {
    public static Logger logger = Logger.getLogger(FireServerHandler.class);
    public static ConcurrentHashMap fireClients = new ConcurrentHashMap();
    public String deviceId;
    public ChannelHandlerContext channelHandlerContext;
    public String randomCode;
    public volatile ScheduledFuture<?> loginChallengeSchedule;
    public volatile ScheduledFuture<?> heartBeatSchedule;
    public volatile int loginChangeTimes;
    public volatile boolean loginSuccess;
    public String ip;
    public volatile int LoseHeartbeatTimes = 0;

    public FireServerHandler() {
        loginChangeTimes = 0;
        loginSuccess = false;
        logger.debug("FireServerHandler()");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try {
            NettyMessage message = (NettyMessage) msg;
            FireDataResolver fireDataResolver = new FireDataResolver();

            //没登录，设备直接给服务器发数据，踢掉---有可能是恶意攻击，加入黑名单
            if (!loginSuccess && message.getHeader().getTypes() != MessageTypeResp.LOGIN_Resp.value()) {
                Jedis jedis = RedisUtil.getJedis();
                jedis.set(ip, ip);
                jedis.expire(ip, Integer.parseInt(PropertyUtils.getValue("BlacklistTimeout")));
                loginChallengeSchedule.cancel(true);
                fireDataResolver = new FireDataResolver();
                logger.debug(ctx.channel().remoteAddress()+" didn't login,go into the blacklist!");
                ctx.writeAndFlush(fireDataResolver.buildInfoResp("didn't login,go into the blacklist!")).addListener(ChannelFutureListener.CLOSE);
            }
            //处理数据
            fireDataResolver.handleData(this, ctx, message);

        } catch (Exception e) {
            heartBeatSchedule.cancel(true);
            loginChallengeSchedule.cancel(true);
            logger.error(e);
            ctx.close();
        }
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        try {
            logger.debug("fire client(" + ctx.channel().remoteAddress() + ") comming! at:" + ctx.channel().localAddress());
            channelHandlerContext = ctx;
            //若加入的设备在黑名单中，直接踢出
            Jedis jedis = RedisUtil.getJedis();
            InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
            ip = address.getAddress().getHostAddress();
            boolean exist = jedis.exists(ip);
            //判断新加入的设备是否在黑名单中
            if (!exist) {
                loginChallengeSchedule = ctx.executor().scheduleAtFixedRate(new LoginChallengeTask(this, RandomCode.genRandomNum(8)), 0, Integer.parseInt(PropertyUtils.getValue("LoginChallengeInterval")) * 1000, TimeUnit.MILLISECONDS);
            } else {
                FireDataResolver fireDataResolver = new FireDataResolver();
                ctx.writeAndFlush(fireDataResolver.buildInfoResp("already in blacklist,please login later!")).addListener(ChannelFutureListener.CLOSE);
            }
        } catch (Exception e) {
            logger.error(e);
            heartBeatSchedule.cancel(true);
            loginChallengeSchedule.cancel(true);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.debug(ctx.channel().remoteAddress() + "          fire client exit!");
        heartBeatSchedule.cancel(true);
        loginChallengeSchedule.cancel(true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        fireClients.remove(deviceId);
        ctx.close();
        logger.error(cause);
        heartBeatSchedule.cancel(true);
        loginChallengeSchedule.cancel(true);
    }



    private static void sendListing(final ChannelHandlerContext ctx, final int index) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK);
        //response.headers().set(CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set(CONTENT_TYPE, "text/plain; charset=UTF-8");
        StringBuilder buf = new StringBuilder();
        JSONObject json = new JSONObject();
        JSONObject member1 = new JSONObject();
        member1.put("loginname", "zhangfan");
        member1.put("password", "userpass");
        member1.put("email", "10371443@qq.com");
        member1.put("sign_date", "2007-06-12");
        buf.append("success_jsonpCallback(");
        buf.append(member1.toString());
        buf.append(")");
        ByteBuf buffer = Unpooled.copiedBuffer(buf, CharsetUtil.UTF_8);
        response.content().writeBytes(buffer, buf.toString().getBytes().length);
        logger.debug(buf.toString());
        buffer.release();
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        WebServerHandler.webClients.remove("" + index);
    }

    public String parseJson(String jsonString) throws JSONException {
        //String jsonString="{\"users\":[{\"loginname\":\"zhangfan\",\"password\":\"userpass\",\"email\":\"10371443@qq.com\"},{\"loginname\":\"zf\",\"password\":\"userpass\",\"email\":\"822393@qq.com\"}]}";
        JSONObject json = new JSONObject(jsonString);
        JSONArray jsonArray = json.getJSONArray("users");
        String loginNames = "loginname list:";
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject user = (JSONObject) jsonArray.get(i);
            String userName = (String) user.get("loginname");
            if (i == jsonArray.length() - 1) {
                loginNames += userName;
            } else {
                loginNames += userName + ",";
            }
        }
        return loginNames;
    }

    public String jsonTest() throws JSONException {
        JSONObject json = new JSONObject();
        JSONArray jsonMembers = new JSONArray();
        JSONObject member1 = new JSONObject();
        member1.put("loginname", "zhangfan");
        member1.put("password", "userpass");
        member1.put("email", "10371443@qq.com");
        member1.put("sign_date", "2007-06-12");
        jsonMembers.put(member1);

        JSONObject member2 = new JSONObject();
        member2.put("loginname", "zf");
        member2.put("password", "userpass");
        member2.put("email", "8223939@qq.com");
        member2.put("sign_date", "2008-07-16");
        jsonMembers.put(member2);
        json.put("users", jsonMembers);

        return json.toString();
    }

    public String writeXml() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Document document = DocumentHelper.createDocument();
        Element root = document.addElement("request");
        Element cma = root.addElement("cma").addAttribute("id", "CMA编号");
        cma.addElement("ip").setText("192.168.1.3");
        cma.addElement("curtime").addText(sdf.format(new Date()));
        cma.addElement("batteryvoltage").setText("0.30");
        cma.addElement("operationtemperature").setText("15.00");
        cma.addElement("floatingcharge").setText("CHARGE");
        return document.asXML();
    }

    public void parseXml(String xml) {
        Document doc = null;
        try {
            doc = DocumentHelper.parseText(xml); // 将字符串转为XML
            Element rootElt = doc.getRootElement(); // 获取根节点
            String EventType = rootElt.attributeValue("EventType");// 拿到根节点的属性
            System.out.println(EventType);
            if (EventType.equals("DeviceNetWorkStatus")) {
                Element Target = rootElt.element("Target");
                String DvrID = Target.attributeValue("DvrID");
                String ChannelID = rootElt.elementText("ChannelID");
                Element Context = rootElt.element("Context");
                String IRStatu = Context.attributeValue("IRStatu");
                String VIStatu = Context.attributeValue("VIStatu");
                System.out.println("DvrID:" + DvrID + " ChannelID:" + ChannelID + " IRStatu:" + IRStatu + " VIStatu:" + VIStatu);
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
