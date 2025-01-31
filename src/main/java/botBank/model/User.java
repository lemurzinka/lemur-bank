package botBank.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * User represents a user entity in the application, containing user details such as
 * personal information, associated cards, accounts, and user state. This entity is used
 * to manage user-related operations and interactions within the system.
 */

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Card> cards;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Account> accounts;


    @Column(nullable = false, name = "telegram_id", unique = true)
    private Long telegramId;


    @Column( name = "first_name")
    @NotNull(message = "first name can not be null")
    private String firstName;


    @Column(name = "last_name")
    private String lastName;


    @Column(name = "phone_number", unique = true)
    private String number;

    @Column(unique = true)
    private String email;

    @Column(updatable = false, name = "created_at")
    private LocalDateTime createdAt;


    @Column(name = "is_admin", nullable = false)
    private boolean isAdmin  = false;

    private Integer stateId;

    @Column(name = "is_banned", nullable = false)
    private boolean isBanned = false;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "transaction_detail_id")
    private TransactionDetail transactionDetail;

    @Column(name = "password", nullable = true)
    private String password;


    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public User(Long telegramId, Integer state){
        this.telegramId = telegramId;
        this.stateId= state;
    }

    public User(Long telegramId, Integer stateId, boolean isAdmin){
        this.telegramId = telegramId;
        this.stateId = stateId;
        this.isAdmin = isAdmin;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", telegramId=" + telegramId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", number='" + number + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", isAdmin=" + isAdmin +
                ", stateId=" + stateId +
                ", isBanned=" + isBanned +
                '}';
    }
}
