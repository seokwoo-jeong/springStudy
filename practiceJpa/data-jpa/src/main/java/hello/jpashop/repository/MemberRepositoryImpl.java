package hello.jpashop.repository;

import java.util.List;

import javax.persistence.EntityManager;

import hello.jpashop.entity.Member;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom{
	
	private final EntityManager em;
	
	@Override
	public List<Member> findMemberCustom() {
		return em.createQuery("select m from Member m", Member.class).getResultList();
	}

	
	
}
