package spittr.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.authentication.rememberme.InMemoryTokenRepositoryImpl;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.util.regex.Pattern;

@Configuration
@EnableWebMvcSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().requireCsrfProtectionMatcher(new RequestMatcher() {
            private RegexRequestMatcher apiMatcher =
                    new RegexRequestMatcher("/api/.*", null);
            private Pattern allowedMethods =
                    Pattern.compile("^(GET|HEAD|TRACE|OPTIONS)$");
            @Override
            public boolean matches(HttpServletRequest request) {
                // CSRF disabled on allowedMethod
                if(allowedMethods.matcher(request.getMethod()).matches())
                    return false;

                // CSRF disabled on api calls
                if (apiMatcher.matches(request))
                    return false;
                // CSRF enables for other requests
                return true;
            }
        }).and()
                .formLogin()
                .loginPage("/login")// if I add /login mapping in the controller will overide the orgin processing
                //.successHandler()
                //.loginProcessingUrl("/login")
                //.defaultSuccessUrl("/spittles/ownSpittles") //shortup for successhandler
                //.failureHandler()
                //.failureUrl("/login-error") //shortcut for (or override) failureHandler, can also set the url to "/login?error=true", default is "/login?error"
                .and()
                .logout()
                .logoutSuccessUrl("/")
                //.logoutSuccessHandler()  //will override the logoutSuccessUrl
                .and()
                .rememberMe()
                .tokenRepository(new InMemoryTokenRepositoryImpl())
                .tokenValiditySeconds(2419200)
                .key("spittrKey")
                .and()
                .httpBasic()
                .realmName("Spittr")
                .and()
                .authorizeRequests()
                .antMatchers("/login").permitAll()
                .antMatchers("/resources/**").permitAll()
           /*     .antMatchers("/").authenticated()
                .antMatchers("/spitter/me").authenticated()
                .antMatchers(HttpMethod.POST, "/spittles").authenticated()*/
                .antMatchers("/spitter/supermanheng27")
                .access("isAuthenticated() and principal.username=='supermanheng27'")
                .anyRequest().authenticated();
    }

/*  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth
      .inMemoryAuthentication()
        .withUser("user").password("password").roles("USER");
  }*/

    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth
                .jdbcAuthentication()
                .dataSource(dataSource).usersByUsernameQuery(
                "select username, password, true " + "from Spitter where username=?")
                .authoritiesByUsernameQuery(
                        "select username, 'ROLE_USER' from Spitter where username=?")
                .passwordEncoder(new BCryptPasswordEncoder());
        //TODO can make BCryptPasswordEncoder a bean in rootconfig
    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
