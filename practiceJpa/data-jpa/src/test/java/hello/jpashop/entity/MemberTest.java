package hello.jpashop.entity;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import hello.jpashop.entity.Member;
import hello.jpashop.entity.Team;
import hello.jpashop.repository.MemberRepository;

@SpringBootTest
@Transactional
public class MemberTest {

	
	@PersistenceContext
	EntityManager em;
	
	@Autowired
	MemberRepository memberRepository;
	
	
	//@Test
	void testEntity(){
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);
		
		Member member1 = new Member("member1",10,teamA);
		Member member2 = new Member("member2",20,teamB);
		Member member3 = new Member("member3",30,teamA);
		Member member4 = new Member("member4",40,teamB);
		
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);
		
		//초기화
		em.flush();
		em.clear();
		
		//확인
		List<Member> members = em.createQuery("select m from Member m",Member.class).getResultList();
		
		for(Member member:members) {
			System.out.println("member = " + member);
			System.out.println("-> member.team = " + member.getTeam());
		}
	}
	
	@Test
	void JpaEventBaseEntity() throws InterruptedException {
		Member member = new Member("member1",10);
		this.memberRepository.save(member);
		
		Thread.sleep(100);
		member.setUsername("member2");
		
		em.flush();
		em.clear();
		
		Member findMember = this.memberRepository.findById(member.getId()).get();
		
		System.out.println("create = " + findMember.getCreateDate());
		System.out.println("update = " + findMember.getLastModifiedDate());
		//System.out.println("create by = " + findMember.getCreatedBy());
		//System.out.println("update by = " + findMember.getLastModifiedBy());
	}
	
	
}
