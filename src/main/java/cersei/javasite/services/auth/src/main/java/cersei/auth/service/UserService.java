package cersei.auth.service;

import cersei.auth.model.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    public void saveUser(User user);
    public User getByUsername(String username) throws UsernameNotFoundException;
}
