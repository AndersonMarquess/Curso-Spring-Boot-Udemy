package com.andersonmarques.cursomc.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
public class Produto implements Serializable {
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;
	private String nome;
	private Double preco;

	//Essa anotação corrige o problema de referencia cíclica, informando que a "referencia da frente já foi chamada".
	@JsonIgnore
	//Anotação para definir relacionamento Muitos para muitos, Cria uma entidade chamada Produto_Categoria,
	//onde ela faz o join do produto_id onde ele for igual ao categoria_id
	@ManyToMany
	@JoinTable(name="PRODUTO_CATEGORIA",
		joinColumns = @JoinColumn(name="produto_id"),
		inverseJoinColumns = @JoinColumn(name="categoria_id")
	)
	private List<Categoria> categorias = new ArrayList<>();

	@JsonIgnore
	//HashSet para evitar item repetido
	//Aqui o ID é do ItemPedidoPK(foi instanciado como id dentro da classe ItemPedido), e de dentro dele pegamos o produto
	@OneToMany(mappedBy="id.produto")
	private Set<ItemPedido> itens = new HashSet<>();	
	
	public Produto() {}

	public Produto(Integer id, String nome, Double preco) {
		super();
		this.id = id;
		this.nome = nome;
		this.preco = preco;
	}
	
	//Não serializa e evita dependência cíclica
	@JsonIgnore
	public List<Pedido> getPedidos(){
		List<Pedido> lista = new ArrayList<>();
		for(ItemPedido p: itens) {
			lista.add(p.getPedido());
		}
		return lista;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public Double getPreco() {
		return preco;
	}

	public void setPreco(Double preco) {
		this.preco = preco;
	}

	public List<Categoria> getCategorias() {
		return categorias;
	}

	public void setCategorias(List<Categoria> categorias) {
		this.categorias = categorias;
	}

	public Set<ItemPedido> getItens() {
		return itens;
	}

	public void setItens(Set<ItemPedido> itens) {
		this.itens = itens;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Produto other = (Produto) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
