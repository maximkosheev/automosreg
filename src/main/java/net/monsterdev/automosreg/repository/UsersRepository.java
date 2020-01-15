package net.monsterdev.automosreg.repository;

import net.monsterdev.automosreg.domain.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class UsersRepository {

  @PersistenceContext
  private EntityManager em;

  public User register(User user) {
    em.persist(user);
    return user;
  }

  public List<User> findAll() {
    return em.createQuery("from User", User.class).getResultList();
  }

  public Long getCount() {
    return  (Long)em.createQuery("SELECT COUNT(u) FROM User u").getSingleResult();
  }

  public User update(User currentUser) {
    return em.merge(currentUser);
  }

  public User loadUserData(User user) {
    Query query = em.createQuery("SELECT u FROM User u JOIN FETCH u.trades t WHERE u.id = :id");
    query.setParameter("id", user.getId());
    return (User) query.getSingleResult();
  }
}
