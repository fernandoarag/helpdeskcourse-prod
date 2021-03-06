package com.fernando.helpdesk.services;

import java.util.List;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.fernando.helpdesk.domain.Pessoa;
import com.fernando.helpdesk.domain.Tecnico;
import com.fernando.helpdesk.domain.dtos.TecnicoDTO;
import com.fernando.helpdesk.repositories.PessoaRepository;
import com.fernando.helpdesk.repositories.TecnicoRepository;
import com.fernando.helpdesk.services.exceptions.DataIntegrityViolationException;
import com.fernando.helpdesk.services.exceptions.ObjectnotFoundException;

@Service
public class TecnicoService {

	@Autowired
	private TecnicoRepository tecnicoRepository;
	@Autowired
	private PessoaRepository pessoaRepository;
	@Autowired
	private BCryptPasswordEncoder encoder;

	public Tecnico findById(Integer id) {
		Optional<Tecnico> objTecnico = tecnicoRepository.findById(id);
		return objTecnico.orElseThrow(() -> new ObjectnotFoundException("Objeto não encontrado! id: " + id));
	}

	public List<Tecnico> findAll() {
		return tecnicoRepository.findAll();
	}

	public Tecnico create(TecnicoDTO objDTO) {
		objDTO.setId(null);
		objDTO.setSenha(encoder.encode(objDTO.getSenha()));
		validaPorCpfEEmail(objDTO);
		Tecnico newObj = new Tecnico(objDTO);
		return tecnicoRepository.save(newObj);
	}

	public Tecnico update(Integer id, @Valid TecnicoDTO objDTO) {
		objDTO.setId(id);
		Tecnico oldObj = findById(id);

		if (!objDTO.getSenha().equals(oldObj.getSenha()))
			objDTO.setSenha(encoder.encode(objDTO.getSenha()));

		validaPorCpfEEmail(objDTO);
		oldObj = new Tecnico(objDTO);
		return tecnicoRepository.save(oldObj);
	}

	public void delete(Integer id) {
		Tecnico obj = findById(id);
		if (obj.getChamados().size() > 0) {
			throw new DataIntegrityViolationException(
					"O técnico possui ordens de serviço vinculadas e não pode ser deletado!");
		}
		tecnicoRepository.deleteById(id);
	}

	private void validaPorCpfEEmail(TecnicoDTO objDTO) {
		Optional<Pessoa> objPessoa = pessoaRepository.findByCpf(objDTO.getCpf());
		if (objPessoa.isPresent() && objPessoa.get().getId() != objDTO.getId()) {
			throw new DataIntegrityViolationException("CPF já cadastrado no sistema!");
		}

		objPessoa = pessoaRepository.findByEmail(objDTO.getEmail());
		if (objPessoa.isPresent() && objPessoa.get().getId() != objDTO.getId()) {
			throw new DataIntegrityViolationException("Email já cadastrado no sistema!");
		}
	}

}
