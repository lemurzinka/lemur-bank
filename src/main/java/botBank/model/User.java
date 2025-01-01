package botBank.model;

import lombok.Data;
import lombok.NoArgsConstructor;


import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Data @NoArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards;

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





}
