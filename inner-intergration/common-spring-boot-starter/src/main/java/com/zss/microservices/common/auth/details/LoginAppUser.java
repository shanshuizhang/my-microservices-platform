package com.zss.microservices.common.auth.details;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.zss.microservices.common.model.SysRole;
import com.zss.microservices.common.model.SysUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author fuguozhang
 * @email fuguozhang@jyblife.com
 * @date 2019/12/11 15:07
 */
@Getter
@Setter
public class LoginAppUser extends SysUser implements UserDetails {
    private static final long serialVersionUID = -3685249101751401211L;

    private Set<SysRole> sysRoles;

    private Set<String> permissions;

    /***
     * 权限重写
     */
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> collection = new HashSet<>();
        if (!CollectionUtils.isEmpty(sysRoles)) {
            sysRoles.parallelStream().forEach(role -> {
                if (role.getCode().startsWith("ROLE_")) {
                    collection.add(new SimpleGrantedAuthority(role.getCode()));
                } else {
                    collection.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
                }
            });
        }

        if (!CollectionUtils.isEmpty(permissions)) {
            permissions.parallelStream().forEach(per -> {
                collection.add(new SimpleGrantedAuthority(per));
            });
        }

        return collection;
    }


    @JsonIgnore
    public Collection<? extends GrantedAuthority> putAll( Collection<GrantedAuthority> collections) {
        Collection<GrantedAuthority> collection = new HashSet<>();

        collection.addAll(collections) ;

        return collection;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return getEnabled();
    }
}
