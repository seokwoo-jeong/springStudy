package hello.jpashop.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.print.Pageable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import hello.jpashop.dto.MemberDto;
import hello.jpashop.entity.Member;
import hello.jpashop.entity.Team;
import hello.jpashop.repository.MemberRepository;

@SpringBootTest
@Transactional
public class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	TeamRepository teamRepository;

	@PersistenceContext
	EntityManager em;

	// @Test
	void testMember() {
		Member member = new Member("memberA");
		Member savedMember = this.memberRepository.save(member);

		Member findMember = this.memberRepository.findById(savedMember.getId()).get();

		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		assertThat(findMember).isEqualTo(member);
	}

	// @Test
	void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberRepository.save(member1);
		memberRepository.save(member2);

		// 단건 조회 검증
		Member findMember1 = memberRepository.findById(member1.getId()).get();
		Member findMember2 = memberRepository.findById(member2.getId()).get();
		assertThat(findMember1).isEqualTo(member1);
		assertThat(findMember2).isEqualTo(member2);

		// 리스트 조회 검증
		List<Member> all = this.memberRepository.findAll();
		assertThat(all.size()).isEqualTo(2);

		long count = this.memberRepository.count();
		assertThat(count).isEqualTo(2);

		// 삭제 검증
		this.memberRepository.delete(member1);
		this.memberRepository.delete(member2);

		long deletedCount = this.memberRepository.count();
		assertThat(deletedCount).isEqualTo(0);
	}

	// @Test
	void findByUsernameAndAgeGreaterThen() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);

		this.memberRepository.save(m1);
		this.memberRepository.save(m2);

		List<Member> result = this.memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);
		assertThat(result.get(0).getUsername()).isEqualTo("AAA");
		assertThat(result.get(0).getAge()).isEqualTo(20);
		assertThat(result.size()).isEqualTo(1);

	}

	// @Test
	void findUsernameList() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("AAA", 20);

		this.memberRepository.save(m1);
		this.memberRepository.save(m2);

		List<String> result = this.memberRepository.findUsernameList();
		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	// @Test
	void findMemberDto() {
		Team team = new Team("Team1");
		teamRepository.save(team);

		Member m1 = new Member("AAA", 10);
		m1.setTeam(team);

		this.memberRepository.save(m1);

		List<MemberDto> result = this.memberRepository.findMemberDto();
		for (MemberDto s : result) {
			System.out.println("s = " + s);
		}
	}

	// @Test
	void findByNames() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);

		this.memberRepository.save(m1);
		this.memberRepository.save(m2);

		List<Member> result = this.memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
		for (Member s : result) {
			System.out.println("s = " + s);
		}
	}

	// @Test
	void returnType() {
		Member m1 = new Member("AAA", 10);
		Member m2 = new Member("BBB", 20);

		this.memberRepository.save(m1);
		this.memberRepository.save(m2);

		List<Member> result1 = this.memberRepository.findByUsername("AAA");
		Member result2 = memberRepository.findMemberByUsername("AAA");
		Optional<Member> result3 = memberRepository.findOptionalByUsername("AAA");

		System.out.println("--------------------------------");
		System.out.println(result1);
		System.out.println(result2);
		System.out.println(result3);
		System.out.println("--------------------------------");
	}

	// @Test
	public void paging() {
		this.memberRepository.save(new Member("member1", 10));
		this.memberRepository.save(new Member("member2", 10));
		this.memberRepository.save(new Member("member3", 10));
		this.memberRepository.save(new Member("member4", 10));
		this.memberRepository.save(new Member("member5", 10));

		int age = 10;
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.DESC, "username"));

		Page<Member> page = this.memberRepository.findByAge(age, pageRequest);

		Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));

		List<Member> content = page.getContent();

		assertThat(content.size()).isEqualTo(3); // 조회된 데이터 수
		assertThat(page.getTotalElements()).isEqualTo(5); // 전체 데이터 수
		assertThat(page.getNumber()).isEqualTo(0); // 페이지 번호
		assertThat(page.getTotalPages()).isEqualTo(2); // 전체 페이지 번호
		assertThat(page.isFirst()).isTrue(); // 첫번째 항목인가?
		assertThat(page.hasNext()).isTrue(); // 다음 페이지가 있는가?
	}

	// @Test
	void bulkUpdate() {
		this.memberRepository.save(new Member("member1", 10));
		this.memberRepository.save(new Member("member2", 19));
		this.memberRepository.save(new Member("member3", 20));
		this.memberRepository.save(new Member("member4", 30));
		this.memberRepository.save(new Member("member5", 40));

		int resultCount = this.memberRepository.bulkAgePlus(20);
		// em.flush();
		// em.clear();

		List<Member> result = memberRepository.findByUsername("member5");
		Member member = result.get(0);
		System.out.println("age = " + member.getAge());

		assertThat(resultCount).isEqualTo(3);

	}

	//@Test
	void findMemberLazy() {
		// given
		// member1 -> teamA
		// member2 -> teamB
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");

		teamRepository.save(teamA);
		teamRepository.save(teamB);

		memberRepository.save(new Member("member1", 10, teamA));
		memberRepository.save(new Member("member2", 20, teamB));

		em.flush();
		em.clear();

		// when
		List<Member> members = memberRepository.findEntityGraphByUsername("member1");

		// then
		for (Member member : members) {
			member.getTeam().getName();
		}
	}

	//@Test
	void queryHint() {
		Member member = new Member("member1", 10);
		memberRepository.save(member);
		em.flush();
		em.clear();
		
		Member findMember = this.memberRepository.findReadOnlyByUsername("member1");
		findMember.setUsername("member2");
		
		em.flush();
	}
	
	@Test
	void lock() {
		Member member = new Member("member1", 10);
		memberRepository.save(member);
		em.flush();
		em.clear();
		
		List<Member> result= this.memberRepository.findLockByUsername("member1");
	}

}
