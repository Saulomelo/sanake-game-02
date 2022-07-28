package br.com.mvbos.lgj;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import br.com.mvbos.lgj.base.CenarioPadrao;
import br.com.mvbos.lgj.base.Elemento;
import br.com.mvbos.lgj.base.Texto;
import br.com.mvbos.lgj.base.Util;

public class JogoCenarioCampanha extends CenarioPadrao {

	enum Estado {
		JOGANDO, GANHOU, PERDEU
	}

	private static final int _LARG = 25;

	private static final int RASTRO_INICIAL = 5;  // tamanho inicial da cobra

	private static  Color X = Color.YELLOW;

	private static Color Y = Color.ORANGE;
	
	private static Color Z = Color.GRAY;

	private int dx = 0, dy = 1; // inicia para baixo

	private boolean moveu;

	private int temporizador = 0;

	private int contadorRastro = RASTRO_INICIAL;

	private Elemento fruta;

	private Elemento serpente;
	
	private Elemento[] nivel;

	private Elemento[] rastros;

	private Texto texto = new Texto(new Font("Arial", Font.PLAIN, 25));

	private Random rand = new Random();
	
	private int nivelParaCarregar = 0;  // deffine qual nivel var carrega

	private int CobraX = 200;   // define a coordenada X da cobra na tela em PIXELS (0 - 450)
	private int CobraY = 200;
	
	// Frutas para finalizar o level
	private int dificuldade = 5;

	private int contadorNivel = 0;
	
	private int pontuacao = 0;

	private Estado estado = Estado.JOGANDO;

	public JogoCenarioCampanha(int largura, int altura) {
		super(largura, altura);
	}

	@Override
	public void carregar() {
		
				
				rastros = new Elemento[dificuldade + RASTRO_INICIAL];
				
			// Cor RANDOMICA da cobra
				int x = ThreadLocalRandom.current().nextInt(100, 255 + 1);
				int y = ThreadLocalRandom.current().nextInt(100, 255 + 1);
				int z = ThreadLocalRandom.current().nextInt(100, 255 + 1);
				
				X = new Color(x, y, z);
				Y = new Color(y, z, x);
				
			// Com RANDOMICA do obstaculo
				int w = ThreadLocalRandom.current().nextInt(0, 2 + 1);
				
				if(w == 0) Z = Color.GRAY;
				if(w == 1) Z = Color.LIGHT_GRAY;
				if(w == 2) Z = Color.ORANGE.darker();
			
				
				fruta = new Elemento(0, 0, _LARG, _LARG);
				fruta.setCor(Color.RED);

				serpente = new Elemento(CobraX, CobraY, _LARG, _LARG);
				serpente.setAtivo(true);
				serpente.setCor(X);
				serpente.setVel(Jogo.velocidade);

				for (int i = 0; i < rastros.length; i++) {
					rastros[i] = new Elemento(serpente.getPx(), serpente.getPy(), _LARG, _LARG);
					rastros[i].setCor(Y);
					rastros[i].setAtivo(true);
				}

				char[][] nivelSelecionado = Nivel.niveis[nivelParaCarregar];   // carregando o nivel 0
				nivel = new Elemento[nivelSelecionado.length * 2];

				for (int linha = 0; linha < nivelSelecionado.length; linha++) {
					for (int coluna = 1; coluna < nivelSelecionado[0].length; coluna++) {
						if (nivelSelecionado[linha][coluna] != ' ') {

							Elemento e = new Elemento();
							e.setAtivo(true);
							e.setCor(Z);

							e.setPx(_LARG * coluna);
							e.setPy(_LARG * linha);

							e.setAltura(_LARG);
							e.setLargura(_LARG);

							nivel[contadorNivel++] = e;
						}
					}
				}
	}
				

	@Override
	public void descarregar() {
		fruta = null;
		rastros = null;
		serpente = null;
		nivelParaCarregar = 0;
	}
	
	@Override
	public void atualizar() {

		if (estado != Estado.JOGANDO) {
			return;
		}

		if (!moveu) {
			if (dy != 0) {
				if (Jogo.controleTecla[Jogo.Tecla.ESQUERDA.ordinal()]) {
					dx = -1;

				} else if (Jogo.controleTecla[Jogo.Tecla.DIREITA.ordinal()]) {
					dx = 1;
				}

				if (dx != 0) {
					dy = 0;
					moveu = true;
				}

			} else if (dx != 0) {
				if (Jogo.controleTecla[Jogo.Tecla.CIMA.ordinal()]) {
					dy = -1;
				} else if (Jogo.controleTecla[Jogo.Tecla.BAIXO.ordinal()]) {
					dy = 1;
				}

				if (dy != 0) {
					dx = 0;
					moveu = true;
				}
			}
		}

		if (temporizador >= 20) {
			temporizador = 0;
			moveu = false;

			int x = serpente.getPx();
			int y = serpente.getPy();

			serpente.setPx(serpente.getPx() + _LARG * dx);
			serpente.setPy(serpente.getPy() + _LARG * dy);

			if (Util.saiu(serpente, largura, altura)) {
				serpente.setAtivo(false);
				criarTelaID();
				estado = Estado.PERDEU;

			} else {

				// colisao com cenario
				for (int i = 0; i < contadorNivel; i++) {
					if (Util.colide(serpente, nivel[i])) {
						serpente.setAtivo(false);
						criarTelaID();
						estado = Estado.PERDEU;
						break;
					}
				}

				// colisao com o rastro
				for (int i = 0; i < contadorRastro; i++) {
					if (Util.colide(serpente, rastros[i])) {
						serpente.setAtivo(false);
						criarTelaID();
						estado = Estado.PERDEU;
						break;
					}
				}
			}

			if (Util.colide(fruta, serpente)) {
				// Adiciona uma pausa
				temporizador = -10;
				contadorRastro++;
				
				if(Jogo.velocidade == 12) // dificil
					pontuacao+=5;
				
				if(Jogo.velocidade == 8) // medio
					pontuacao+=3;
				
				if(Jogo.velocidade == 4) // fácil
					pontuacao+=2;
				
				
				fruta.setAtivo(false);
				

				if (contadorRastro == rastros.length) {                   //fim de nível
					
				
					nivelParaCarregar++;
					contadorRastro = 5;
					contadorNivel = 0;
					
					if(nivelParaCarregar == 6) {
						serpente.setAtivo(false);
						estado = Estado.GANHOU;
					}
					//se o (nivelParaCarregar > "tamanho niveis disponiveis - 1")
				
					
					if(nivelParaCarregar == 1) {  //nivel 2
						
						
						CobraX = 100; 
						CobraY = 100;
						
						dx = 1; 	
						dy = 0; 

						carregar();
					}
					
					if(nivelParaCarregar == 2) {  //nivel 3
						
						
						CobraX = 250; 
						CobraY = 200;
						
						dx = 0; 	
						dy = -1;		
						carregar();
					}
					
					if(nivelParaCarregar == 3) {  //nivel 4
						
						
						CobraX = 300; 
						CobraY = 350;
						
						dx = -1; 
						dy = 0; 
						
						carregar();
					}
					
					if(nivelParaCarregar == 4) {  //nivel 5
						
						
						CobraX = 200; 
						CobraY = 250;
						
						dx = 0; 
						dy = 1; 
						
						carregar();
					}
					
					if(nivelParaCarregar == 5) {  //nivel 6
						
						
						CobraX = 200; 
						CobraY = 150;
						
						dx = -1; 
						dy = 0; 
						
						carregar();
					}
				
					
					if(nivelParaCarregar == 6) { // carrega a tela de preencher o nome do player
						criarTelaID();
					}
					
				}

			}

			for (int i = 0; i < contadorRastro; i++) {
				Elemento rastro = rastros[i];
				int tx = rastro.getPx();
				int ty = rastro.getPy();

				rastro.setPx(x);
				rastro.setPy(y);

				x = tx;
				y = ty;
			}

		} else {
			temporizador += serpente.getVel();
		}

		// Adicionando frutas
		if (estado == Estado.JOGANDO && !fruta.isAtivo()) {
			int x = rand.nextInt(largura / _LARG);
			int y = rand.nextInt(altura / _LARG);

			fruta.setPx(x * _LARG);
			fruta.setPy(y * _LARG);
			fruta.setAtivo(true);

			// colisao com a serpente
			if (Util.colide(fruta, serpente)) {
				fruta.setAtivo(false);
				return;
			}

			// colisao com rastro
			for (int i = 0; i < contadorRastro; i++) {
				if (Util.colide(fruta, rastros[i])) {
					fruta.setAtivo(false);
					return;
				}
			}

			// colisao com cenario
			for (int i = 0; i < contadorNivel; i++) {
				if (Util.colide(fruta, nivel[i])) {
					fruta.setAtivo(false);
					return;
				}
			}

		}

	}

	@Override
	public void desenhar(Graphics2D g) {

		if (fruta.isAtivo()) {
			fruta.desenha(g);
		}

		for (Elemento e : nivel) {
			if (e == null)
				break;

			e.desenha(g);
		}

		for (int i = 0; i < contadorRastro; i++) {
			rastros[i].desenha(g);
		}

		serpente.desenha(g);

		g.setColor(Color.yellow);
		texto.desenha(g, String.valueOf(rastros.length - contadorRastro), largura - 35, altura);
		texto.desenha(g, String.valueOf(pontuacao), largura - 35, 20);

		if (estado != Estado.JOGANDO) {

			if (estado == Estado.GANHOU) {
				
				imprimeRank(g);
				
			}	
			else
				imprimeRank(g);
		}

		if (Jogo.pausado)
			Jogo.textoPausa.desenha(g, "PAUSA", largura / 2 - Jogo.textoPausa.getFonte().getSize(), altura / 2);
	}
	
	public void limpaTela(Graphics2D g) {
		
		g.setColor(Color.GREEN.darker().darker());					// cor do fundo
		g.fillRect(0, 0, largura, altura);
	
	}
	
		public void imprimeRank(Graphics2D g) {
		
			limpaTela(g);
			
			g.setColor(Color.yellow);
			texto.desenha(g, "Ranking", 190, 30);
			
			String cbx = "Posição     Nome     Score";
			texto.desenha(g, cbx , 80, 70);
			
			int t = 120;
			
			for(int i = 0; i < rank.size();i++) {
				
				String str1 = (i+1) + "°"; 
				String str2 = rank.get(i).getNome();
				String str3 = Integer.toString(rank.get(i).getPontuacao());
						
				texto.desenha(g, str1 , 110, t);
				texto.desenha(g, str2 , 200, t);
				texto.desenha(g, str3 , 320, t);
				t += 35;
				
			}
		}
	
	
	List<Player> rank = new ArrayList<>();
	
	public void criarTelaID(){
		
		
		//File reader
		
			//carrega oq tem dentro do arquivo para a lista
			try {
				FileInputStream is  = new FileInputStream("C:\\Users\\saulo\\Downloads\\rank.txt");
				InputStreamReader ir = new InputStreamReader(is);
				BufferedReader in = new BufferedReader(ir);
				
				while(in.ready()) {
					String str = in.readLine();
					
					if (!str.equals("")) {
						String[] dadosJogador = str.split(" ");
						rank.add(new Player(dadosJogador[0], Integer.parseInt(dadosJogador[1])));
					}
				}
				in.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
		//Caixa de preencher o id do jogador
				JFrame frame = new JFrame("Insira o seu nome");
				JPanel panel = new JPanel();
				frame.setContentPane(panel);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				
				JLabel rotulo = new JLabel();
				rotulo.setText(" Nome : ") ;
				panel.add(rotulo);
				
				JTextField textField = new JTextField (40) ;
				panel.add(textField);
				
				JButton button = new JButton ("Salvar");
				panel.add(button);
				
				
				button.addActionListener(new ActionListener() {
					
					public void actionPerformed(ActionEvent e) {
						JButton button = (JButton) e.getSource();
						button.setText("salvo");
						frame.setVisible(false);
						
						String nome = textField.getText();
						
						rank.add(new Player(nome, pontuacao));
						
						if (rank.size() > 1) {
							
							Collections.sort(rank, (Player o1, Player o2) -> sort(o1,o2));
						}
						
			
					//File writer	
						try {
							FileWriter writer = new FileWriter("C:\\Users\\saulo\\Downloads\\rank.txt");
							BufferedWriter buffer = new BufferedWriter(writer); 
							
							for(int i = 0; i < rank.size(); i++) {
								buffer.write(rank.get(i).getNome() + " " + rank.get(i).getPontuacao() + "\n");
							} 
							buffer.close();
							
						} catch (IOException e1) {
									e1.printStackTrace();
						}  
		
						
							
							}
						});
				
				frame.setContentPane(panel);
				
				frame.pack();
				frame.setVisible(true);
	}
	
	private int sort(Player o1,Player o2) {
		
		if(o1.getPontuacao() > o2.getPontuacao()) 
			return -1;
		else if(o1.getPontuacao() < o2.getPontuacao()) 
			return 1;
		return 0;	
	}
	
}