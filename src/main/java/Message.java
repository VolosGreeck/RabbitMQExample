import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;
import java.time.LocalDate;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class Message implements Serializable {

    private String user;
    private String subject;
    private String content;
    @XmlJavaTypeAdapter(LocalDateAdapter.class)
    private LocalDate date;

    public Message(String user, String subject, String content, LocalDate date) {
        this.user = user;
        this.subject = subject;
        this.content = content;
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    //for JAXBContext.newInstance()
    Message() {
    }

    @Override
    public String toString() {
        return "{" +
                "user='" + user + '\'' +
                ", subject='" + subject + '\'' +
                ", content='" + content + '\'' +
                ", date=" + date +
                '}';
    }

}
