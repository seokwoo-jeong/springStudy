package study.querydsl.repository;

import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;
import study.querydsl.entity.Member;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public List<MemberTeamDto> search(MemberSearchCondition condition) {
		return this.queryFactory
				.select(new QMemberTeamDto(member.id.as("memberId"), member.username, member.age, team.id.as("teamId"),
						team.name.as("teamName")))
				.from(member).leftJoin(member.team, team)
				.where(usernameEq(condition.getUsername()), teamNameEq(condition.getTeamName()),
						ageGoe(condition.getAgeGoe()), ageLoe(condition.getAgeLoe()))
				.fetch();
	}

	private BooleanExpression ageBetween(int ageLoe, int ageGoe) {
		return ageLoe(ageLoe).and(ageGoe(ageGoe));
	}

	private BooleanExpression usernameEq(String username) {
		if (StringUtils.hasText(username)) {
			return member.username.eq(username);
		}
		return null;
	}

	private BooleanExpression teamNameEq(String teamName) {
		if (StringUtils.hasText(teamName)) {
			return member.team.name.eq(teamName);
		}
		return null;
	}

	private BooleanExpression ageGoe(Integer ageGoe) {
		if (ageGoe != null) {
			return member.age.goe(ageGoe);
		}
		return null;
	}

	private BooleanExpression ageLoe(Integer ageLoe) {
		if (ageLoe != null) {
			return member.age.loe(ageLoe);
		}
		return null;
	}

	public List<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) {
		return this.queryFactory
				.select(new QMemberTeamDto(member.id.as("memberId"), 
										   member.username, 
										   member.age, 
										   team.id.as("teamId"),
										   team.name.as("teamName")))
				.from(member)
				.leftJoin(member.team, team)
				.where(usernameEq(condition.getUsername()), 
					   teamNameEq(condition.getTeamName()),
					   ageGoe(condition.getAgeGoe()), 
					   ageLoe(condition.getAgeLoe())
				)
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();
		
		//fetchCount, fetchResult는 더이상 지원안해서 searchPageComplex방식으로만 사용해야 함
	}

	@Override
	public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {
		List<MemberTeamDto> content = this.queryFactory
				.select(new QMemberTeamDto(member.id.as("memberId"), 
										   member.username, 
										   member.age, 
										   team.id.as("teamId"),
										   team.name.as("teamName")))
				.from(member)
				.leftJoin(member.team, team)
				.where(usernameEq(condition.getUsername()), 
					   teamNameEq(condition.getTeamName()),
					   ageGoe(condition.getAgeGoe()), 
					   ageLoe(condition.getAgeLoe())
				)
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();
		
		long total = queryFactory.select(member)
				 .from(member)
				 .leftJoin(member.team, team)
				 .where(usernameEq(condition.getUsername()), 
						   teamNameEq(condition.getTeamName()),
						   ageGoe(condition.getAgeGoe()), 
						   ageLoe(condition.getAgeLoe())
				 ).fetch().size();
		
		//page개수 최적화
		//페이지 할 정도의 데이터가 존재하지 않으면 count쿼리가 날라가지 않음
		JPAQuery<Member> countQuery = queryFactory.select(member)
								 .from(member)
								 .leftJoin(member.team, team)
								 .where(usernameEq(condition.getUsername()), 
										   teamNameEq(condition.getTeamName()),
										   ageGoe(condition.getAgeGoe()), 
										   ageLoe(condition.getAgeLoe())
								 );
		
		return PageableExecutionUtils.getPage(content, pageable, ()->countQuery.fetch().size()); 
		//return new PageImpl<>(content,pageable, total);
	}
}
