package exchange.velox.authservice.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Present email request information.
 */
public class EmailRequestDTO implements Serializable {

    private List<EmailOptionDTO> emailContent;
    private UserDTO author;
    private ReferenceDTO refObject;

    public enum TemplateType {
        userLocked,
        forgotPassword,
        changedPassword,
        initiatedPassword,
        memberRegistration
    }

    public List<EmailOptionDTO> getEmailContent() {
        return emailContent;
    }

    public void setEmailContent(List<EmailOptionDTO> emailContent) {
        this.emailContent = emailContent;
    }

    public UserDTO getAuthor() {
        return author;
    }

    public void setAuthor(UserDTO author) {
        this.author = author;
    }

    public ReferenceDTO getRefObject() {
        return refObject;
    }

    public void setRefObject(ReferenceDTO refObject) {
        this.refObject = refObject;
    }

    @Override
    public String toString() {
        return "EmailRequestDTO{" +
                    "emailContent=" + emailContent +
                    ", author=" + author +
                    ", refObject=" + refObject +
                    '}';
    }
}
