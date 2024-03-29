package study.querydsl.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

import static study.querydsl.entity.QTeam.*;
import static study.querydsl.entity.QMember.*;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {
	
	private final EntityManager em;
	private final JPAQueryFactory queryFactory;

	
	public void save(Member member) {
		em.persist(member);
	}
	
	public Optional<Member> findById(Long id){
		Member findMember =  em.find(Member.class, id);
		return Optional.ofNullable(findMember);
	}
	
	public List<Member> findAll(){
		return em.createQuery("select m from Member m",Member.class)
				.getResultList();
	}
	
	public List<Member> findAll_querydsl(){
		return queryFactory.selectFrom(member).fetch();
	}
	
	public List<Member> findByUsername(String username){
		return em.createQuery("select m from Member m where m.username = :username",Member.class)
				.setParameter("username", username)
				.getResultList();
	}
	
	public List<Member> findByUsername_querydsl(String username){
		return this.queryFactory
				.select(member)
				.from(member)
				.where(member.username.eq(username))
				.fetch();
	}	
	
	public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){
		BooleanBuilder builder = new BooleanBuilder();
		
		if(StringUtils.hasText(condition.getUsername())) {
			builder.and(member.username.eq(condition.getUsername()));
		}
		
		if(StringUtils.hasText(condition.getTeamName())) {
			builder.and(member.team.name.eq(condition.getTeamName()));
		}
		
		if(condition.getAgeGoe() != null) {
			builder.and(member.age.goe(condition.getAgeGoe()));
		}
		
		if(condition.getAgeLoe() != null) {
			builder.and(member.age.loe(condition.getAgeLoe()));
		}
		
		return this.queryFactory.select(new QMemberTeamDto(
				member.id.as("memberId"),
				member.username,
				member.age,
				team.id.as("teamId"),
				team.name.as("teamName")
				))
				.from(member)
				.leftJoin(member.team,team)
				.where(builder)
				.fetch();
	}
	
	public List<MemberTeamDto> search(MemberSearchCondition condition){
		return this.queryFactory.select(new QMemberTeamDto(
										member.id.as("memberId"),
										member.username,
										member.age,
										team.id.as("teamId"),
										team.name.as("teamName")))
				.from(member)
				.leftJoin(member.team,team)
				.where(
						usernameEq(condition.getUsername()),
						teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()),
						ageLoe(condition.getAgeLoe()))
				.fetch();
	}
	
	private BooleanExpression ageBetween(int ageLoe, int ageGoe) {
		return ageLoe(ageLoe).and(ageGoe(ageGoe));
	}

	private BooleanExpression usernameEq(String username) {
		if(StringUtils.hasText(username)) {
			return member.username.eq(username);
		}
		return null;
	}

	private BooleanExpression teamNameEq(String teamName) {
		if(StringUtils.hasText(teamName)) {
			return member.team.name.eq(teamName);
		}
		return null;
	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		if(ageGoe != null) {
			return member.age.goe(ageGoe);
		}
		return null;
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		if(ageLoe != null) {
			return member.age.loe(ageLoe);
		}
		return null;
	}
	
}
