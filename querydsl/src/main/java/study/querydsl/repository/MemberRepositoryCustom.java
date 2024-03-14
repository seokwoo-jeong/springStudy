package study.querydsl.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;

public interface MemberRepositoryCustom {

	
	List<MemberTeamDto> search(MemberSearchCondition condition);
	
	//List<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable);
	
	Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable);
}
