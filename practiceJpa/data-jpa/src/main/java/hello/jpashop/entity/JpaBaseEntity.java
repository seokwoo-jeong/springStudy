package hello.jpashop.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.Getter;

@Getter
@MappedSuperclass	//데이터만 공유하게 해주는 어노테이션
public class JpaBaseEntity { 
	
	@Column(updatable = false)
	private LocalDateTime createDate;
	
	private LocalDateTime updateDate;

	@PrePersist
	public void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createDate = now;
		this.updateDate = now;
	}
	
	@PreUpdate
	public void preUpdate() {
		this.updateDate = LocalDateTime.now();
	}
}
