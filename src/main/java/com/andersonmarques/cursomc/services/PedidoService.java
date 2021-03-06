package com.andersonmarques.cursomc.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.andersonmarques.cursomc.domain.Cliente;
import com.andersonmarques.cursomc.domain.ItemPedido;
import com.andersonmarques.cursomc.domain.PagamentoComBoleto;
import com.andersonmarques.cursomc.domain.Pedido;
import com.andersonmarques.cursomc.domain.enums.EstadoPagamento;
import com.andersonmarques.cursomc.repositories.ItemPedidoRepository;
import com.andersonmarques.cursomc.repositories.PagamentoRepository;
import com.andersonmarques.cursomc.repositories.PedidoRepository;
import com.andersonmarques.cursomc.security.UserSS;
import com.andersonmarques.cursomc.services.exceptions.AuthorizationException;
import com.andersonmarques.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class PedidoService {
	
	//Essa anotação Autowired instância automaticamente a classe PedidoRepository
	@Autowired 
	private PedidoRepository repositorio;
	@Autowired
	private PagamentoRepository pagamentoRepository;
	@Autowired
	private ProdutoService produtoService;
	@Autowired
	private BoletoService boletoService;
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	@Autowired
	private ClienteService clienteService;
	@Autowired
	private EmailService emailService;

	//Faz a busca no repositório com base no id
	public Pedido find(Integer id) {
		Optional<Pedido> objetoRecebido = repositorio.findById(id);
		
		//Se o objeto não for encontrado, é lançado uma exception através de uma lambda para informar o problema.
		return objetoRecebido.orElseThrow(()-> new ObjectNotFoundException("O Objeto não foi contrado, ID: "+id+
				", Pedido: "+Pedido.class.getName()));
	}

	@Transactional
	public Pedido insert(Pedido obj) {
		obj.setId(null);
		obj.setInstante(new Date());
		obj.setCliente(clienteService.find(obj.getCliente().getId()));
		obj.getPagamento().setEstadoPagamento(EstadoPagamento.PENDENTE);
		obj.getPagamento().setPedido(obj);
		
		//Se o pagamento do obj é uma instância de PagamentoComBoleto
		if (obj.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) obj.getPagamento();
			boletoService.preeencherPagamentoComBoleto(pagto, obj.getInstante());
		}
		
		repositorio.save(obj);
		pagamentoRepository.save(obj.getPagamento());
		for (ItemPedido ip :obj.getItens()) {
			ip.setDesconto(0d);
			ip.setProduto(produtoService.find(ip.getProduto().getId()));
			ip.setPreco(ip.getProduto().getPreco());
			ip.setPedido(obj);
		}
		itemPedidoRepository.saveAll(obj.getItens());
		emailService.sandOrderConfirmationEmail(obj);
		return obj;
	}
	
	//Buscar informações das categorias dividido em paginação
	public Page<Pedido> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		UserSS user = UserService.authenticated();
		if(user == null) {
			throw new AuthorizationException("Usuário não autenticado");
		}
		
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		
		Cliente cliente = clienteService.find(user.getId());
		
		return repositorio.findByCliente(cliente, pageRequest);
	}
}
