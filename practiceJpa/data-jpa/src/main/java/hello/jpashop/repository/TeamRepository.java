package hello.jpashop.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import hello.jpashop.entity.Member;
import hello.jpashop.entity.Team;

public interface TeamRepository extends JpaRepository<Team,Long>{

	
}
