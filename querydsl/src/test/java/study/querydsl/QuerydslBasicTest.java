package study.querydsl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;
import static com.querydsl.jpa.JPAExpressions.*;

import java.util.List;

import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.config.location=classpath:application-test.yml")
public class QuerydslBasicTest {

	@Autowired
	EntityManager em;

	JPAQueryFactory queryFactory;

	@BeforeEach
	void before() {
		queryFactory = new JPAQueryFactory(em);

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

	// @Test
	void startJPQL() {
		// member1 find
		Member findMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
				.setParameter("username", "member1").getSingleResult();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	// @Test
	void startQuerydsl() {
		Member findMember = queryFactory.select(member).from(member).where(member.username.eq("member1")).fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	// @Test
	void search() {
		Member findMember = queryFactory.selectFrom(member).where(member.username.eq("member1").and(member.age.eq(10)))
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	// @Test
	void searchAndParam() {
		Member findMember = queryFactory.selectFrom(member).where(member.username.eq("member1"), member.age.eq(10))
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	// @Test
	void resultFetch() {
		// List<Member> fetch = queryFactory.selectFrom(member).fetch();

		// Member fetchOne = queryFactory.selectFrom(member).fetchOne();

		// Member fetchFirst = queryFactory.selectFrom(member).fetchFirst();

		QueryResults<Member> results = queryFactory.selectFrom(member).fetchResults();

		results.getTotal();
		List<Member> content = results.getResults();

		long total = queryFactory.selectFrom(member).fetchCount();
	}

	/*
	 * 회원 나이 내림차순(desc) 회원 이름 오름차순 (asc) 회원 이름 없을 경우 마지막에 출력
	 */
	// @Test
	void sort() {
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));

		List<Member> result = queryFactory.selectFrom(member).where(member.age.eq(100))
				.orderBy(member.age.desc(), member.username.asc().nullsLast()).fetch();

		Member member5 = result.get(0);
		Member member6 = result.get(1);
		Member memberNull = result.get(2);

		assertThat(member5.getUsername()).isEqualTo("member5");
		assertThat(member6.getUsername()).isEqualTo("member6");
		assertThat(memberNull.getUsername()).isNull();
	}

	// @Test
	void paging1() {
		List<Member> result = queryFactory.selectFrom(member).orderBy(member.username.desc()).offset(1).limit(2)
				.fetch();

		assertThat(result.size()).isEqualTo(2);
	}

	// @Test
	void paging2() {
		QueryResults<Member> queryResults = queryFactory.selectFrom(member).orderBy(member.username.desc()).offset(1)
				.limit(2).fetchResults();

		assertThat(queryResults.getTotal()).isEqualTo(4);
		assertThat(queryResults.getLimit()).isEqualTo(2);
		assertThat(queryResults.getOffset()).isEqualTo(1);
		assertThat(queryResults.getResults().size()).isEqualTo(2);
	}

	// @Test
	void aggregation() {
		List<Tuple> result = queryFactory
				.select(member.count(), member.age.sum(), member.age.avg(), member.age.max(), member.age.min())
				.from(member).fetch();

		Tuple tuple = result.get(0);
		assertThat(tuple.get(member.count())).isEqualTo(4);
		assertThat(tuple.get(member.age.sum())).isEqualTo(100);
		assertThat(tuple.get(member.age.avg())).isEqualTo(25);
		assertThat(tuple.get(member.age.max())).isEqualTo(40);
		assertThat(tuple.get(member.age.min())).isEqualTo(10);
	}

	/*
	 * 팀의 이름과 각 팀의 평균 연령
	 */

	// @Test
	void group() {
		List<Tuple> result = queryFactory.select(team.name, member.age.avg()).from(member).join(member.team, team)
				.groupBy(team.name).fetch();

		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);

		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);

		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);
	}

	/*
	 * teamA에 소속된 모든 회원
	 */
	// @Test
	void join() {
		List<Member> result = queryFactory.selectFrom(member).join(member.team, team).where(team.name.eq("teamA"))
				.fetch();

		assertThat(result).extracting("username").containsExactly("member1", "member2");

	}

	/*
	 * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회 jpql : select m, t from Member m
	 * left join m.team t on t.name = 'teamA'
	 */
	// @Test
	void join_on_filtering() {
		List<Tuple> result = queryFactory.select(member, team).from(member).leftJoin(member.team, team)
				.on(team.name.eq("teamA")).fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	@PersistenceUnit
	EntityManagerFactory emf;

	// @Test
	void fetchJoinNo() {
		em.flush();
		em.clear();
		Member findMember = queryFactory.selectFrom(member).where(member.username.eq("member1")).fetchOne();

		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).isFalse();
	}

	// @Test
	void fetchJoinUse() {
		em.flush();
		em.clear();
		Member findMember = queryFactory.selectFrom(member).join(member.team, team).fetchJoin()
				.where(member.username.eq("member1")).fetchOne();

		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("패치 조인 적용").isTrue();
	}

	/*
	 * 나이가 가장 많은 회원 조회
	 */
	// @Test
	void subQuery() {
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory.selectFrom(member)
				.where(member.age.eq(select(memberSub.age.max()).from(memberSub))).fetch();

		assertThat(result).extracting("age").containsExactly(40);
	}

	/*
	 * 나이가 평균 이상인 회원
	 */
	// @Test
	void subQueryGoe() {
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory.selectFrom(member)
				.where(member.age.goe(select(memberSub.age.avg()).from(memberSub))).fetch();

		assertThat(result).extracting("age").containsExactly(30, 40);
	}

	// @Test
	void subQueryIn() {
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory.selectFrom(member)
				.where(member.age.in(select(memberSub.age).from(memberSub).where(memberSub.age.gt(10)))).fetch();

		assertThat(result).extracting("age").containsExactly(20, 30, 40);
	}

	// @Test
	void selectSubQuery() {
		QMember memberSub = new QMember("memberSub");
		List<Tuple> result = queryFactory.select(member.username, select(memberSub.age.avg()).from(memberSub))
				.from(member).fetch();

		for (Tuple tuple : result) {
			System.out.println("selectSubQuery tuple = " + tuple);
		}
	}

	// @Test
	void basicCase() {
		List<String> result = queryFactory.select(member.age.when(10).then("열살").when(20).then("스무살").otherwise("기타"))
				.from(member).fetch();

		for (String r : result) {
			System.out.println(r);
		}

	}

	// @Test
	void complexCase() {
		List<String> result = queryFactory.select(new CaseBuilder().when(member.age.between(0, 20)).then("0~20살")
				.when(member.age.between(21, 30)).then("21~30살").otherwise("기타")).from(member).fetch();

		for (String r : result) {
			System.out.println(r);
		}
	}

	// @Test
	void constant() {
		List<Tuple> result = queryFactory.select(member.username, Expressions.constant("A")).from(member).fetch();

		for (Tuple t : result) {
			System.out.println(t);
		}
	}

	// @Test
	void concat() {
		List<String> results = queryFactory.select(member.username.concat("_").concat(member.age.stringValue()))
				.from(member).where(member.username.eq("member1")).fetch();

		for (String result : results) {
			System.out.println("concat = " + result);
		}
	}

	// @Test
	void simpleProjection() {
		List<String> result = queryFactory.select(member.username).from(member).fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	// @Test
	void tupleProjection() {
		List<Tuple> result = queryFactory.select(member.username, member.age).from(member).fetch();

		for (Tuple tuple : result) {
			String username = tuple.get(member.username);
			String age = tuple.get(member.age).toString();

			System.out.println("username = " + username);
			System.out.println("age = " + age);
		}
	}

	// @Test
	void findDtoByJPQL() {
		List<MemberDto> result = em
				.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m",
						MemberDto.class)
				.getResultList();

		for (MemberDto memberDto : result) {
			System.out.println("memberDTO = " + memberDto);
		}
	}

	// @Test
	void findDtoBySetter() {
		// bean 사용 시 getter setter를 이용하여 값이 꽃힌다.
		List<MemberDto> result = queryFactory.select(Projections.bean(MemberDto.class, member.username, member.age))
				.from(member).fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	// @Test
	void findDtoByField() {
		// fields 사용 시 값이 필드에 꽃힌다. (getter,setter 필요없음)
		List<MemberDto> result = queryFactory.select(Projections.fields(MemberDto.class, member.username, member.age))
				.from(member).fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	// @Test
	void findDtoByConstructor() {
		// Constructor 생성자 접근방법
		List<MemberDto> result = queryFactory
				.select(Projections.constructor(MemberDto.class, member.username, member.age)).from(member).fetch();

		for (MemberDto memberDto : result) {
			System.out.println("Constructor memberDto = " + memberDto);
		}
	}

	// @Test
	void findUserDto() {
		// field명과 엔티티 필드명이 다를 경우 as사용해야 한다. 안그러면 null값으로 나온다
		List<UserDto> result = queryFactory
				.select(Projections.fields(UserDto.class, member.username.as("name"), member.age)).from(member).fetch();

		for (UserDto userDto : result) {
			System.out.println("memberDto = " + userDto);
		}
	}

	// @Test
	void findUserDtoBySubQuery() {
		QMember memberSub = new QMember("memberSub");

		// field명과 엔티티 필드명이 다를 경우 as사용해야 한다. 안그러면 null값으로 나온다
		List<UserDto> result = queryFactory.select(Projections.fields(UserDto.class, member.username.as("name"),

				ExpressionUtils.as(JPAExpressions.select(memberSub.age.max()).from(memberSub), "age"))).from(member)
				.fetch();

		for (UserDto userDto : result) {
			System.out.println("memberDto = " + userDto);
		}
	}

	// @Test
	void findDtoByQueryProjection() {
		List<MemberDto> result = queryFactory.select(new QMemberDto(member.username, member.age)).from(member).fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	// @Test
	void dynamicQuery_BooleanBuilder() {
		String usernameParam = "member1";
		Integer ageParam = null;

		List<Member> result = searchMember1(usernameParam, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember1(String usernameParam, Integer ageParam) {
		BooleanBuilder builder = new BooleanBuilder();
		if (usernameParam != null) {
			builder.and(member.username.eq(usernameParam));
		}

		if (ageParam != null) {
			builder.and(member.age.eq(ageParam));

		}
		return queryFactory.selectFrom(member).where(builder).fetch();
	}

	// @Test
	void dynamicQuery_whereParam() {
		String usernameParam = "member1";
		Integer ageParam = 10;

		List<Member> result = searchMember2(usernameParam, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}

	private List<Member> searchMember2(String usernameParam, Integer ageParam) {
		return queryFactory.selectFrom(member).where(allEq(usernameParam, ageParam)).fetch();
	}

	private BooleanExpression usernameEq(String usernameParam) {
		if (!StringUtils.hasText(usernameParam))
			return null;
		return member.username.eq(usernameParam);
	}

	private BooleanExpression ageEq(Integer ageParam) {
		return ageParam != null ? member.age.eq(ageParam) : null;
	}

	private BooleanExpression allEq(String username, Integer age) {
		return usernameEq(username).and(ageEq(age));
	}

	// @Test
	void bulkUpdate() {
		long count = queryFactory.update(member).set(member.username, "비회원").where(member.age.lt(28)).execute();

		System.out.println(count);

		em.flush();
		em.clear();
		// 벌크연산하면 db데이터에 업데이트 하기 때문에 db데이터랑 영속성컨텍스트 데이터가 맞지 않는다.
		// 그래서 flush랑 clear 해줘서 다시 데이터를 맞춰버리자.
	}

	// @Test
	void bulkAdd() {
		long count = queryFactory.update(member).set(member.age, member.age.add(1)).execute();

		em.flush();
		em.clear();
	}

	// @Test
	void bulkDelete() {
		long count = queryFactory.delete(member).where(member.age.gt(18)).execute();

		em.flush();
		em.clear();
	}

	// @Test
	void sqlFunction() {
		List<String> result = queryFactory
				.select(Expressions.stringTemplate("function('replace',{0},{1},{2})", member.username, "member", "M"))
				.from(member).fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	@Test
	void sqlFuntion2() {
		List<String> result = queryFactory.select(member.username).from(member)
//				.where(member.username.eq(Expressions.stringTemplate("function('lower', {0})", member.username)))
				.where(member.username.eq(member.username.lower()))
				.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

}
