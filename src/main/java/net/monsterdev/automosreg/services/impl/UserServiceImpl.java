package net.monsterdev.automosreg.services.impl;

import java.util.Date;
import java.util.List;
import net.monsterdev.automosreg.domain.Trade;
import net.monsterdev.automosreg.domain.User;
import net.monsterdev.automosreg.repository.TradesRepository;
import net.monsterdev.automosreg.repository.UsersRepository;
import net.monsterdev.automosreg.services.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("userService")
public class UserServiceImpl implements UserService {

  private static final Logger LOG = LogManager.getLogger(UserServiceImpl.class);

  @Autowired
  private UsersRepository usersRepository;

  @Autowired
  private TradesRepository tradesRepository;

  private User currentUser;

  private List<Trade> getActualTrades() {
    return tradesRepository.findAll(currentUser.getId());
  }

  @Override
  public List<User> findAll() {
    return usersRepository.findAll();
  }

  @Override
  public Long getCount() {
    return usersRepository.getCount();
  }

  @Override
  public User register(User newUser) {
    newUser.setCreateDT(new Date());
    return usersRepository.register(newUser);
  }

  @Override
  public void setCurrentUser(User user) {
    this.currentUser = user;
    this.currentUser.setTrades(getActualTrades());
  }

  @Override
  public User getCurrentUser() {
    return currentUser;
  }

  @Override
  public User update() {
    currentUser = usersRepository.update(currentUser);
    return currentUser;
  }
}
