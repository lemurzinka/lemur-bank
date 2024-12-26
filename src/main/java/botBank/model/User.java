package botBank.model;

import lombok.Data;
import lombok.NoArgsConstructor;


import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDateTime;





@Entity
@Data @NoArgsConstructor
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role = UserRole.USER;

    private Integer stateId;

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

    public User(Long telegramId, Integer stateId, UserRole role){
        this.telegramId = telegramId;
        this.stateId = stateId;
        this.role = role;
    }




}
