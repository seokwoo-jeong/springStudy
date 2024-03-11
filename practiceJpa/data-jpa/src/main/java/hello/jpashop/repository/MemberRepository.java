package hello.jpashop.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import javax.persistence.LockModeType;
import javax.persistence.QueryHint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import hello.jpashop.dto.MemberDto;
import hello.jpashop.entity.Member;

public interface MemberRepository extends JpaRepository<Member,Long>, MemberRepositoryCustom{

	List<Member> findByUsernameAndAgeGreaterThan(String username, int age);
	
	@Query("select m.username from Member m")
	List<String> findUsernameList();
	
	@Query("select new hello.jpashop.dto.MemberDto(m.id, m.username, t.name) from Member m join m.team t")
	List<MemberDto> findMemberDto();
	
	@Query("select m from Member m where m.username in :names")
	List<Member> findByNames(@Param("names") Collection<String> names);
	
	List<Member> findByUsername(String username);	//컬렉션
		
	Member findMemberByUsername(String username);	//단건
	
	Optional<Member> findOptionalByUsername(String username);	//단건 optional
	
	//countquery는 join할 필요없을 경우, 따로 작성해준다.
	//@Query(value = "select m from Member m left join m.team t",
	//		countQuery = "select count(m) from Member m")
	Page<Member> findByAge(int age, Pageable pageable);
	
	@Modifying(clearAutomatically = true)//이거 하면 영속성 컨텍스트 clear 해준다.
	@Query("update Member m set m.age = m.age + 1 where m.age >= :age")
	int bulkAgePlus(@Param("age") int age);
	
	@Query("select m from Member m left join fetch m.team")
	List<Member> findMemberFetchJoin();

	@Override
	@EntityGraph(attributePaths = {"team"})	//fetch join과 동일한 기능으로, findMemberFetchJoin()와 동일
	List<Member> findAll();
	
	
	@EntityGraph(attributePaths = {"team"})	//fetch join과 동일한 기능
	@Query("select m from Member m")
	List<Member> findMemberEntityGraph();
	
	@EntityGraph(attributePaths = {"team"})
	List<Member> findEntityGraphByUsername(@Param("username") String username);

	@QueryHints(value = @QueryHint(name="org.hibernate.readOnly", value = "true"))
	Member findReadOnlyByUsername(String username);
	
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	List<Member> findLockByUsername(String username);
}


