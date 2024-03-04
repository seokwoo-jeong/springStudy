package hello.jpashop.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import hello.jpashop.entity.Member;
import hello.jpashop.repository.MemberJpaRepository;

@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {
	
	@Autowired
	MemberJpaRepository memberJpaRepository;
	
	//@Test
	void testMember() {
		Member member = new Member("memberA");
		Member savedMember = this.memberJpaRepository.save(member);
		
		
		Member findMember = this.memberJpaRepository.find(savedMember.getId());

		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		assertThat(findMember).isEqualTo(member);
	}
	
	//@Test
	void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberJpaRepository.save(member1);
		memberJpaRepository.save(member2);
		
		
		//단건 조회 검증
		Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
		Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
		assertThat(findMember1).isEqualTo(member1);
		assertThat(findMember2).isEqualTo(member2);
		
		//리스트 조회 검증
		List<Member> all = this.memberJpaRepository.findAll();
		assertThat(all.size()).isEqualTo(2);
		
		long count = this.memberJpaRepository.count();
		assertThat(count).isEqualTo(2);
		
		//삭제 검증
		this.memberJpaRepository.delete(member1);
		this.memberJpaRepository.delete(member2);
		
		long deletedCount = this.memberJpaRepository.count();
		assertThat(deletedCount).isEqualTo(0);
	}
	
	//@Test
	void findByUsernameAndAgeGreaterThen() {
		Member m1 = new Member("AAA",10);
		Member m2 = new Member("AAA",20);
		
		this.memberJpaRepository.save(m1);
		this.memberJpaRepository.save(m2);
		
		List<Member> result = this.memberJpaRepository.findByUsernameAndAgeGreaterThen("AAA", 15);
		assertThat(result.get(0).getUsername()).isEqualTo("AAA");
		assertThat(result.get(0).getAge()).isEqualTo(20);
		assertThat(result.size()).isEqualTo(1);
		
	}
	
	//@Test
	public void paging() {
		this.memberJpaRepository.save(new Member("member1",10));
		this.memberJpaRepository.save(new Member("member2",10));
		this.memberJpaRepository.save(new Member("member3",10));
		this.memberJpaRepository.save(new Member("member4",10));
		this.memberJpaRepository.save(new Member("member5",20));
		
		
		int age = 10;
		int offset = 0;
		int limit = 3;
		
		List<Member> members = this.memberJpaRepository.findByPage(age,offset,limit);
		Long totalCount = this.memberJpaRepository.totalCount(age);
		
		assertThat(members.size()).isEqualTo(3);
		assertThat(totalCount).isEqualTo(4);
	}
	
	@Test
	void bulkUpdate() {
		this.memberJpaRepository.save(new Member("member1",10));
		this.memberJpaRepository.save(new Member("member2",19));
		this.memberJpaRepository.save(new Member("member3",20));
		this.memberJpaRepository.save(new Member("member4",30));
		this.memberJpaRepository.save(new Member("member5",40));
		
		int resultCount = this.memberJpaRepository.bulkAgePlus(20);
		
		assertThat(resultCount).isEqualTo(3);
		
	}
}
