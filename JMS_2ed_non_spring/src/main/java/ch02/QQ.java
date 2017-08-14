package ch02;

import javax.jms.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by robin on 2017/8/14.
 */

public class QQ implements MessageListener {
    private TopicConnection connection;
    private TopicSession session;
    private TopicPublisher publisher;
    private TopicSubscriber subscriber;
    private String username;
    public String getUserName(){
        return username;
    }

    public QQ(String factory,String topicName,String username)
            throws NamingException, JMSException {
        //1、得到jndi
        InitialContext jndi=new InitialContext();
        //2、jndi中得到受管对象factory
        TopicConnectionFactory connectionFactory=(TopicConnectionFactory)jndi.lookup(factory);
        //3、工厂中得到连接
        TopicConnection connection=connectionFactory.createTopicConnection();
        //4、连接中得到session
        TopicSession session=connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
        //5、jndi中得到受管对象topic
         Topic topic=(Topic)jndi.lookup(topicName);
        //6、session中创建生产者消费者，并且与topic绑定
        TopicPublisher publisher=session.createPublisher(topic);
        TopicSubscriber subscriber=session.createSubscriber(topic, null, true);
        //7、订阅者设置监听器
        subscriber.setMessageListener(this);
        //8、初始化非受管成员对象
        this.connection=connection;
        this.session=session;
        this.publisher=publisher;
        this.username=username;
        connection.start();
    }
    public void writeMessage(String msg) throws JMSException {
        TextMessage textMessage=session.createTextMessage();
        textMessage.setText(username+"/"+msg);
        publisher.publish(textMessage);
    }

    public void onMessage(Message message) {
        try {
            TextMessage textMessage = (TextMessage) message;
            String msg = textMessage.getText();
            String[] arr=msg.split("/");
            if(arr.length==2)
                System.out.println(arr[0]+":"+arr[1]);
            else if(arr[1].equals(username)){
                System.out.println(arr[0]+"私信对你说："+arr[2]);
            }
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
    public void closeConn() throws JMSException {
        connection.close();
    }
    public static void main(String[] args)
            throws JMSException, NamingException, IOException {
        QQ qq=new QQ(args[0],args[1],args[2]);
        BufferedReader br=new BufferedReader(
                new InputStreamReader(
                        System.in));
        System.out.println("----------QQ的id为："+qq.getUserName()+"-------------");
        while(true){
            String str=br.readLine();
            if(!str.equals("exit")){
                qq.writeMessage(str);
            }else {
                qq.closeConn();
                break;
            }
        }
    }
}
