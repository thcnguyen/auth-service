package exchange.velox.authservice.dto;

import com.fasterxml.jackson.annotation.JsonView;
import exchange.velox.authservice.util.view.Views;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class EmailOptionDTO implements Serializable {
    private String from;
    @JsonView(Views.LogView.class)
    private Set<String> to = new HashSet<>();
    private Set<String> bcc = new HashSet<>();
    private String subject;
    private String template;
    private String body;
    private byte[] file;
    private String attachmentName;
    private String contentType;
    @JsonView(Views.LogView.class)
    private String errorMessage;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Set<String> getTo() {
        return to;
    }

    public void setTo(Set<String> to) {
        this.to = to;
    }

    public Set<String> getBcc() {
        return bcc;
    }

    public void setBcc(Set<String> bcc) {
        this.bcc = bcc;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public byte[] getFile() {
        return file;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        EmailOptionDTO that = (EmailOptionDTO) o;
        return Objects.equals(from, that.from) &&
                    Objects.equals(to, that.to) &&
                    Objects.equals(bcc, that.bcc) &&
                    Objects.equals(subject, that.subject) &&
                    Objects.equals(template, that.template) &&
                    Objects.equals(body, that.body) &&
                    Arrays.equals(file, that.file) &&
                    Objects.equals(attachmentName, that.attachmentName) &&
                    Objects.equals(contentType, that.contentType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(from, to, bcc, subject, template, body, attachmentName, contentType);
        result = 31 * result + Arrays.hashCode(file);
        return result;
    }

    @Override
    public String toString() {
        return "EmailOptionDTO{" +
                    "from='" + from + '\'' +
                    ", to=" + to +
                    ", bcc=" + bcc +
                    ", subject='" + subject + '\'' +
                    ", template='" + template + '\'' +
                    ", attachmentName='" + attachmentName + '\'' +
                    ", contentType='" + contentType + '\'' +
                    '}';
    }
}
