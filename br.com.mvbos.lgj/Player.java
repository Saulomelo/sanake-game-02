package br.com.mvbos.lgj;

public class Player {
	private String nome;
	private int pontuacao;
	
	public Player(String nome, int pontuacao) {
		this.nome = nome;
		this.pontuacao = pontuacao;
	}
	
	public String getNome() {
		return nome;
	}
	
	public int getPontuacao() {
		return pontuacao;
	}
	
	
	
}
