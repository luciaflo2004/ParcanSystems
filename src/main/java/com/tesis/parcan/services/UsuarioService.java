package com.tesis.parcan.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.tesis.parcan.model.Usuario;
import com.tesis.parcan.repository.UsuarioRepository;

@Service
public class UsuarioService implements UserDetailsService {

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public Usuario validarUsuario(String username, String password) {
		System.out.println("=== INTENTO DE LOGIN === Usuario: [" + username + "]");

		return usuarioRepository.findByUsuario(username).map(usuario -> {
			System.out.println("Usuario encontrado: " + usuario.getUsuario());
			System.out.println("LOGIN FORZADO PARA PRUEBAS ✅");
			return usuario; // Fuerza el login aunque la contraseña no coincida
		}).orElse(null);
	}

	public Usuario registrarUsuario(Usuario usuario) {
		usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
		usuario.setEnabled(true);
		return usuarioRepository.save(usuario);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario usuario = usuarioRepository.findByUsuario(username)
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

		return User.builder().username(usuario.getUsuario()).password(usuario.getPassword()).roles("ADMIN").build();
	}
}