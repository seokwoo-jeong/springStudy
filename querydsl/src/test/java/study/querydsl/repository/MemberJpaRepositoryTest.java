package study.querydsl.repository;

import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
public class MemberJpaRepositoryTest {

	
	@Autowired
	EntityManager em;
	
	@Autowired MemberJpaRepository memberJpaRepository;
	
	@BeforeEach
	void before() {

		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");

		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);

		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);
	}
	
	
	//@Test
	void basicTest() {
		Member member = new Member("member1",10);
		memberJpaRepository.save(member);
		
		Member findMember = this.memberJpaRepository.findById(member.getId()).get();
		assertThat(findMember).isEqualTo(member);
		
		List<Member> result1 = this.memberJpaRepository.findAll();
		assertThat(result1).containsExactly(member);
		
		List<Member> result2 = this.memberJpaRepository.findByUsername("member1");
		assertThat(result2).containsExactly(member);
	}
	
	//@Test
	void basicQueryDslTest() {
		Member member = new Member("member1",10);
		memberJpaRepository.save(member);
		
		Member findMember = this.memberJpaRepository.findById(member.getId()).get();
		assertThat(findMember).isEqualTo(member);
		
		List<Member> result1 = this.memberJpaRepository.findAll_querydsl();
		assertThat(result1).containsExactly(member);
		
		List<Member> result2 = this.memberJpaRepository.findByUsername_querydsl("member1");
		assertThat(result2).containsExactly(member);
	}
	
	//@Test
	void searchTest() {
		MemberSearchCondition condition = new MemberSearchCondition();
		condition.setAgeGoe(35);
		condition.setAgeLoe(40);
		condition.setTeamName("teamB");
		
		List<MemberTeamDto> result = this.memberJpaRepository.searchByBuilder(condition);
		
		assertThat(result).extracting("username").containsExactly("member4");
	}
	
	@Test
	void searchTest2() {
		MemberSearchCondition condition = new MemberSearchCondition();
		condition.setAgeGoe(35);
		condition.setAgeLoe(40);
		condition.setTeamName("teamB");
		
		List<MemberTeamDto> result = this.memberJpaRepository.search(condition);
		
		assertThat(result).extracting("username").containsExactly("member4");
	}
}
