import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Random;

public class Sender {

    public static final Logger logger = LogManager.getLogger(Sender.class);

    private static final String DEFAULT_QUEUE = "TaskQueue";
    private static final String NOTIFICATION_QUEUE = "NotifyQueue";


    public static void main(String[] args) throws Exception {
        sendMsg();
    }

    public static void sendMsg() throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        JAXBContext context = JAXBContext.newInstance(Message.class);
        Marshaller marshaller = context.createMarshaller();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare(DEFAULT_QUEUE, false, false, false, null);
        channel.queueDeclare(NOTIFICATION_QUEUE, false, false, false, null);

        Message notificationMessage = new Message();
        while (!"Alphabet is complete!".equals(notificationMessage.getContent())) {
            channel.basicConsume(NOTIFICATION_QUEUE, (consumerTag, delivery) -> notificationMessage.setContent(new String(delivery.getBody())), cons -> {
            });
            Message message = new Message("user", "Alphabet Check", generateContent(), LocalDate.now());
            StringWriter stringWriter = new StringWriter();
            marshaller.marshal(message, stringWriter);
            String result = stringWriter.toString();

            channel.basicPublish("", DEFAULT_QUEUE, MessageProperties.TEXT_PLAIN, result.getBytes());
            logger.info("User sent '" + result + "'");

            Thread.sleep(500);
        }

        channel.queueDelete(NOTIFICATION_QUEUE);
        System.exit(0);
    }

    private static String generateContent() {
        Random random = new Random();
        return random
                .ints(65, 91)
                .mapToObj(randomChar -> (char) randomChar)
                .limit(1)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
}
