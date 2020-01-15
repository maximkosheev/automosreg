package net.monsterdev.automosreg.domain;

import lombok.Data;

import javax.persistence.*;
import java.io.File;
import java.nio.file.Path;

@Entity
@Table(name = "Documents")
@Data
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    @Column(name = "name", length = 50)
    private String name;
    @Column(name = "path")
    private String path;

    @PrePersist
    private void calcName() {
        Path path = (new File(this.path)).toPath();
        this.name = path.getFileName().toString();
    }
}
