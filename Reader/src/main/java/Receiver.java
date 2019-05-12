import com.rabbitmq.client.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.concurrent.TimeoutException;

public class Receiver {

    private static final String DEFAULT_QUEUE = "TaskQueue";
    private static final String NOTIFICATION_QUEUE = "NotifyQueue";
    public static final String ERROR_QUEUE = "ErrorQueue";
    private static final Logger logger = LogManager.getLogger(Receiver.class);

    public static void main(String[] args) throws IOException, TimeoutException, JAXBException {
        ConnectionFactory factory = new ConnectionFactory();
        JAXBContext context = JAXBContext.newInstance(Message.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();

        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.queueDeclare(DEFAULT_QUEUE, false, false, false, null);
        channel.queueDeclare(ERROR_QUEUE, false, false, false, null);
        channel.queueDeclare(NOTIFICATION_QUEUE, false, false, false, null);
        logger.info("Waiting for messages");

        HashSet<String> alphabet = new HashSet<>(26);
        DeliverCallback deliverCallback = (consumerTag, delivery) -> {

            String result = new String(delivery.getBody());
            logger.info("Recieved '" + result + "'");
            Message message;
            try {
                message = (Message) unmarshaller.unmarshal(new StringReader(result));
                logger.info("Recieved '" + message.toString() + "'");
            } catch (JAXBException e) {
                logger.error(e.getMessage());
                channel.basicPublish("", ERROR_QUEUE, MessageProperties.TEXT_PLAIN, e.getMessage().getBytes());
                return;
            }
            if (!alphabet.add(message.getContent())) {
                logger.info("Letter " + message.getContent() + " already in the set");
                channel.basicPublish("", ERROR_QUEUE, MessageProperties.TEXT_PLAIN, ("Letter " + message.getContent() + " already in the set").getBytes());
            } else if (alphabet.size() == 26) {
                channel.basicPublish("", NOTIFICATION_QUEUE, MessageProperties.TEXT_PLAIN, ("Alphabet is complete!").getBytes());
                alphabet.forEach(logger::info);
                alphabet.clear();
                alphabet.forEach(logger::info);
            }
        };

        channel.basicConsume(DEFAULT_QUEUE, true, deliverCallback, cons -> {
        });
    }
}
