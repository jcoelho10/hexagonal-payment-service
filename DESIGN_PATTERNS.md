# 🧭 Índice Navegável

## 🏗️ Padrões Criacionais

1. [Singleton](#11-singleton)
2. [Factory Method](#12-factory-method)
3. [Abstract Factory](#13-abstract-factory)
4. [Builder](#14-builder)
5. [Prototype](#15-prototype)

---

## 🧱 Padrões Estruturais

6. [Adapter](#21-adapter)
7. [Bridge](#22-bridge)
8. [Composite](#23-composite)
9. [Decorator](#24-decorator)
10. [Facade](#25-facade)
11. [Flyweight](#26-flyweight)
12. [Proxy](#27-proxy)

---

## 🔄 Padrões Comportamentais

13. [Strategy](#31-strategy)
14. [Observer](#32-observer)
15. [Command](#33-command)
16. [Chain of Responsibility](#34-chain-of-responsibility)
17. [State](#35-state)
18. [Template Method](#36-template-method)
19. [Mediator](#37-mediator)
20. [Memento](#38-memento)
21. [Iterator](#39-iterator)
22. [Visitor](#310-visitor)

---

## ☕ Java / Spring Boot

* [Padrões mais usados no Java moderno](#-4-quais-padrões-são-mais-usados-em-java-moderno)
* [Mapa Mental](#-5-mapa-mental-rápido)
* [Resumo dos padrões](#-6-resumo-dos-padrões)
* [Como memorizar](#-7-como-memorizar)
* [Entrevistas Java/Spring Boot](#-8-para-entrevistas-javaspring-boot)

---

# 1. 🏗️ Padrões Criacionais

## 1.1 Singleton

### 🎯 Objetivo

Garantir que exista apenas **uma instância** de uma classe.

### 📌 Quando usar

* Configurações globais
* Gerenciadores de logs
* Recursos compartilhados
* Objetos que devem possuir uma única instância

### 💡 Como funciona

O construtor é privado e a classe fornece um método para obter a única instância existente.

### 💻 Exemplo Java

```java
public class Singleton {

    private static Singleton instancia;

    private Singleton() {
    }

    public static Singleton getInstance() {

        if (instancia == null) {
            instancia = new Singleton();
        }

        return instancia;
    }
}
```

### ▶️ Uso

```java
Singleton s1 = Singleton.getInstance();
Singleton s2 = Singleton.getInstance();

System.out.println(s1 == s2); // true
```

### ☕ Java / Spring

No Spring, o escopo padrão de um Bean é `singleton`.

```java
@Service
public class PagamentoService {
}
```

Por padrão, o container Spring gerencia uma única instância desse Bean por `ApplicationContext`.

> ⚠️ Em implementações manuais, é necessário considerar questões de concorrência e thread safety.

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 1.2 Factory Method

### 🎯 Objetivo

Criar objetos sem expor diretamente a lógica de criação.

### 📌 Quando usar

Quando existem vários tipos de objetos que implementam a mesma interface.

### 💡 Como funciona

O cliente solicita um objeto à Factory, que decide qual implementação concreta deve ser criada.

### 💻 Exemplo

```java
public interface Veiculo {

    void mover();
}
```

```java
public class Carro implements Veiculo {

    @Override
    public void mover() {
        System.out.println("Carro andando");
    }
}
```

```java
public class Moto implements Veiculo {

    @Override
    public void mover() {
        System.out.println("Moto andando");
    }
}
```

```java
public class VeiculoFactory {

    public static Veiculo criar(String tipo) {

        if ("carro".equalsIgnoreCase(tipo)) {
            return new Carro();
        }

        if ("moto".equalsIgnoreCase(tipo)) {
            return new Moto();
        }

        throw new IllegalArgumentException(
            "Tipo de veículo inválido"
        );
    }
}
```

### ▶️ Uso

```java
Veiculo veiculo =
        VeiculoFactory.criar("carro");

veiculo.mover();
```

### ☕ Aplicação

Pode ser utilizado quando uma aplicação precisa decidir dinamicamente qual implementação concreta utilizar.

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 1.3 Abstract Factory

### 🎯 Objetivo

Criar **famílias inteiras de objetos relacionados**.

### 📌 Quando usar

Quando existem diferentes famílias de produtos que precisam trabalhar juntas.

### 💡 Exemplo

```text
Windows
 ├── Botão
 └── Janela

Linux
 ├── Botão
 └── Janela
```

### 💻 Produtos

```java
public interface Botao {

    void renderizar();
}
```

```java
public interface Janela {

    void abrir();
}
```

### Produtos Windows

```java
public class BotaoWindows implements Botao {

    @Override
    public void renderizar() {
        System.out.println("Botão Windows");
    }
}
```

```java
public class JanelaWindows implements Janela {

    @Override
    public void abrir() {
        System.out.println("Janela Windows");
    }
}
```

### Produtos Linux

```java
public class BotaoLinux implements Botao {

    @Override
    public void renderizar() {
        System.out.println("Botão Linux");
    }
}
```

```java
public class JanelaLinux implements Janela {

    @Override
    public void abrir() {
        System.out.println("Janela Linux");
    }
}
```

### 🏭 Abstract Factory

```java
public interface GUIFactory {

    Botao criarBotao();

    Janela criarJanela();
}
```

### Factory Windows

```java
public class WindowsFactory implements GUIFactory {

    @Override
    public Botao criarBotao() {
        return new BotaoWindows();
    }

    @Override
    public Janela criarJanela() {
        return new JanelaWindows();
    }
}
```

### Factory Linux

```java
public class LinuxFactory implements GUIFactory {

    @Override
    public Botao criarBotao() {
        return new BotaoLinux();
    }

    @Override
    public Janela criarJanela() {
        return new JanelaLinux();
    }
}
```

### ▶️ Uso

```java
GUIFactory factory =
        new WindowsFactory();

Botao botao =
        factory.criarBotao();

Janela janela =
        factory.criarJanela();

botao.renderizar();
janela.abrir();
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 1.4 Builder

### 🎯 Objetivo

Construir objetos complexos **passo a passo**.

### 📌 Problema

```java
Pessoa pessoa = new Pessoa(
    "João",
    30,
    "SP",
    "Brasil",
    true,
    false
);
```

É difícil entender o significado de cada parâmetro.

### 💡 Solução

```java
Pessoa pessoa = new PessoaBuilder()
        .nome("João")
        .idade(30)
        .cidade("SP")
        .ativo(true)
        .build();
```

### 💻 Exemplo

```java
public class Pessoa {

    private String nome;
    private int idade;
    private String cidade;
    private boolean ativo;

    public Pessoa(
        String nome,
        int idade,
        String cidade,
        boolean ativo
    ) {
        this.nome = nome;
        this.idade = idade;
        this.cidade = cidade;
        this.ativo = ativo;
    }
}
```

```java
public class PessoaBuilder {

    private String nome;
    private int idade;
    private String cidade;
    private boolean ativo;

    public PessoaBuilder nome(String nome) {
        this.nome = nome;
        return this;
    }

    public PessoaBuilder idade(int idade) {
        this.idade = idade;
        return this;
    }

    public PessoaBuilder cidade(String cidade) {
        this.cidade = cidade;
        return this;
    }

    public PessoaBuilder ativo(boolean ativo) {
        this.ativo = ativo;
        return this;
    }

    public Pessoa build() {

        return new Pessoa(
            nome,
            idade,
            cidade,
            ativo
        );
    }
}
```

### ☕ Muito usado em

* Spring
* Lombok
* DTOs
* APIs REST

### Lombok

```java
@Builder
public class Pessoa {

    private String nome;
    private int idade;
    private String cidade;
}
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 1.5 Prototype

### 🎯 Objetivo

Criar objetos através de **clonagem**.

### 📌 Quando usar

Quando criar um objeto é caro ou complexo e podemos reutilizar uma estrutura existente.

### 💻 Exemplo

```java
public class Produto implements Cloneable {

    private String nome;
    private double preco;

    public Produto(String nome, double preco) {
        this.nome = nome;
        this.preco = preco;
    }

    @Override
    public Produto clone() {

        try {
            return (Produto) super.clone();

        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### ▶️ Uso

```java
Produto produtoOriginal =
        new Produto("Notebook", 5000);

Produto copia =
        produtoOriginal.clone();
```

A cópia é criada a partir do objeto existente.

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

# 2. 🧱 Padrões Estruturais

## 2.1 Adapter

### 🎯 Objetivo

Converter uma interface em outra interface esperada pelo cliente.

### 💡 Analogia

🔌 **Adaptador de tomada.**

### 📌 Quando usar

* Sistemas legados
* APIs de terceiros
* Integrações
* Interfaces incompatíveis

### 💻 Exemplo

```java
public interface PagamentoNovo {

    void pagar();
}
```

```java
public class SistemaLegado {

    public void realizarPagamento() {

        System.out.println(
            "Pagamento realizado pelo sistema legado"
        );
    }
}
```

```java
public class PagamentoAdapter
        implements PagamentoNovo {

    private final SistemaLegado legado;

    public PagamentoAdapter(
        SistemaLegado legado
    ) {
        this.legado = legado;
    }

    @Override
    public void pagar() {
        legado.realizarPagamento();
    }
}
```

### ▶️ Uso

```java
SistemaLegado legado =
        new SistemaLegado();

PagamentoNovo pagamento =
        new PagamentoAdapter(legado);

pagamento.pagar();
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 2.2 Bridge

### 🎯 Objetivo

Separar **abstração** da **implementação**.

### 📌 Quando usar

Quando existem duas dimensões que podem variar independentemente.

### 💻 Exemplo

```java
public interface Dispositivo {

    void ligar();

    void desligar();
}
```

```java
public class TV implements Dispositivo {

    @Override
    public void ligar() {
        System.out.println("TV ligada");
    }

    @Override
    public void desligar() {
        System.out.println("TV desligada");
    }
}
```

```java
public class ControleRemoto {

    protected final Dispositivo dispositivo;

    public ControleRemoto(
        Dispositivo dispositivo
    ) {
        this.dispositivo = dispositivo;
    }

    public void ligar() {
        dispositivo.ligar();
    }

    public void desligar() {
        dispositivo.desligar();
    }
}
```

### ▶️ Uso

```java
Dispositivo tv = new TV();

ControleRemoto controle =
        new ControleRemoto(tv);

controle.ligar();
controle.desligar();
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 2.3 Composite

### 🎯 Objetivo

Tratar objetos individuais e grupos de objetos da **mesma forma**.

### 💻 Exemplo

```java
public interface ComponenteArquivo {

    void mostrar();
}
```

```java
public class Arquivo implements ComponenteArquivo {

    private final String nome;

    public Arquivo(String nome) {
        this.nome = nome;
    }

    @Override
    public void mostrar() {
        System.out.println("Arquivo: " + nome);
    }
}
```

```java
import java.util.ArrayList;
import java.util.List;

public class Pasta implements ComponenteArquivo {

    private final String nome;

    private final List<ComponenteArquivo> itens =
            new ArrayList<>();

    public Pasta(String nome) {
        this.nome = nome;
    }

    public void adicionar(ComponenteArquivo item) {
        itens.add(item);
    }

    @Override
    public void mostrar() {

        System.out.println("Pasta: " + nome);

        itens.forEach(
            ComponenteArquivo::mostrar
        );
    }
}
```

### ▶️ Uso

```java
Pasta raiz =
        new Pasta("Documentos");

raiz.adicionar(
    new Arquivo("contrato.pdf")
);

raiz.adicionar(
    new Arquivo("curriculo.pdf")
);

raiz.mostrar();
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 2.4 Decorator

### 🎯 Objetivo

Adicionar funcionalidades a um objeto **sem alterar sua classe original**.

### ☕ Exemplo

```text
Café
 └── Café + Leite
      └── Café + Chocolate
```

### 💻 Interface

```java
public interface Cafe {

    String preparar();

    double custo();
}
```

```java
public class CafeSimples implements Cafe {

    @Override
    public String preparar() {
        return "Café";
    }

    @Override
    public double custo() {
        return 5.00;
    }
}
```

```java
public abstract class CafeDecorator
        implements Cafe {

    protected final Cafe cafe;

    protected CafeDecorator(Cafe cafe) {
        this.cafe = cafe;
    }
}
```

```java
public class LeiteDecorator
        extends CafeDecorator {

    public LeiteDecorator(Cafe cafe) {
        super(cafe);
    }

    @Override
    public String preparar() {
        return cafe.preparar() + " + leite";
    }

    @Override
    public double custo() {
        return cafe.custo() + 1.50;
    }
}
```

```java
public class ChocolateDecorator
        extends CafeDecorator {

    public ChocolateDecorator(Cafe cafe) {
        super(cafe);
    }

    @Override
    public String preparar() {
        return cafe.preparar() + " + chocolate";
    }

    @Override
    public double custo() {
        return cafe.custo() + 2.00;
    }
}
```

### ▶️ Uso

```java
Cafe cafe =
        new ChocolateDecorator(
            new LeiteDecorator(
                new CafeSimples()
            )
        );

System.out.println(cafe.preparar());
System.out.println(cafe.custo());
```

### ☕ Java

O conceito de Decorator aparece em várias classes de `java.io` e em wrappers de streams.

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 2.5 Facade

### 🎯 Objetivo

Fornecer uma interface simples para um sistema complexo.

### 💻 Exemplo

```java
public class TV {

    public void ligar() {
        System.out.println("TV ligada");
    }
}
```

```java
public class Som {

    public void ligar() {
        System.out.println("Som ligado");
    }
}
```

```java
public class Luz {

    public void diminuir() {
        System.out.println("Luz diminuída");
    }
}
```

```java
public class HomeTheaterFacade {

    private final TV tv;
    private final Som som;
    private final Luz luz;

    public HomeTheaterFacade(
        TV tv,
        Som som,
        Luz luz
    ) {
        this.tv = tv;
        this.som = som;
        this.luz = luz;
    }

    public void assistirFilme() {

        tv.ligar();
        som.ligar();
        luz.diminuir();

        System.out.println(
            "Filme iniciado"
        );
    }
}
```

### ▶️ Uso

```java
HomeTheaterFacade facade =
        new HomeTheaterFacade(
            new TV(),
            new Som(),
            new Luz()
        );

facade.assistirFilme();
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 2.6 Flyweight

### 🎯 Objetivo

Compartilhar objetos para **economizar memória**.

### 💻 Exemplo

```java
public class ArvoreTipo {

    private final String especie;
    private final String textura;

    public ArvoreTipo(
        String especie,
        String textura
    ) {
        this.especie = especie;
        this.textura = textura;
    }

    public void renderizar(int x, int y) {

        System.out.println(
            "Árvore " + especie +
            " em " + x + "," + y
        );
    }
}
```

```java
import java.util.HashMap;
import java.util.Map;

public class ArvoreFactory {

    private static final Map<String, ArvoreTipo>
            tipos = new HashMap<>();

    public static ArvoreTipo getTipo(
            String especie,
            String textura) {

        return tipos.computeIfAbsent(
            especie,
            key -> new ArvoreTipo(
                especie,
                textura
            )
        );
    }
}
```

### ▶️ Uso

```java
ArvoreTipo carvalho1 =
        ArvoreFactory.getTipo(
            "Carvalho",
            "carvalho.png"
        );

ArvoreTipo carvalho2 =
        ArvoreFactory.getTipo(
            "Carvalho",
            "carvalho.png"
        );

System.out.println(
    carvalho1 == carvalho2
); // true
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 2.7 Proxy

### 🎯 Objetivo

Controlar o acesso a um objeto real.

### 💻 Exemplo

```java
public interface Imagem {

    void exibir();
}
```

```java
public class ImagemReal implements Imagem {

    private final String arquivo;

    public ImagemReal(String arquivo) {

        this.arquivo = arquivo;

        carregar();
    }

    private void carregar() {

        System.out.println(
            "Carregando " + arquivo
        );
    }

    @Override
    public void exibir() {

        System.out.println(
            "Exibindo " + arquivo
        );
    }
}
```

```java
public class ImagemProxy implements Imagem {

    private final String arquivo;

    private ImagemReal imagemReal;

    public ImagemProxy(String arquivo) {
        this.arquivo = arquivo;
    }

    @Override
    public void exibir() {

        if (imagemReal == null) {

            imagemReal =
                new ImagemReal(arquivo);
        }

        imagemReal.exibir();
    }
}
```

### ▶️ Uso

```java
Imagem imagem =
        new ImagemProxy("foto.png");

imagem.exibir();
```

### ☕ Spring

O conceito de Proxy aparece em:

* `@Transactional`
* AOP
* Segurança
* Interceptors

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

# 3. 🔄 Padrões Comportamentais

## 3.1 Strategy

### 🎯 Objetivo

Trocar algoritmos ou regras de negócio em tempo de execução.

### 💻 Exemplo

```java
public interface PagamentoStrategy {

    void pagar(double valor);
}
```

```java
public class PixStrategy
        implements PagamentoStrategy {

    @Override
    public void pagar(double valor) {

        System.out.println(
            "Pagamento PIX: " + valor
        );
    }
}
```

```java
public class CartaoStrategy
        implements PagamentoStrategy {

    @Override
    public void pagar(double valor) {

        System.out.println(
            "Pagamento cartão: " + valor
        );
    }
}
```

```java
public class BoletoStrategy
        implements PagamentoStrategy {

    @Override
    public void pagar(double valor) {

        System.out.println(
            "Pagamento boleto: " + valor
        );
    }
}
```

### ▶️ Uso

```java
PagamentoStrategy strategy =
        new PixStrategy();

strategy.pagar(100);
```

### ☕ Spring Boot

É especialmente útil para separar diferentes regras de negócio por estratégia.

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 3.2 Observer

### 🎯 Objetivo

Notificar objetos quando alguma informação ou estado muda.

### 💻 Exemplo

```java
public interface Observer {

    void atualizar(String mensagem);
}
```

```java
public class Inscrito implements Observer {

    private final String nome;

    public Inscrito(String nome) {
        this.nome = nome;
    }

    @Override
    public void atualizar(String mensagem) {

        System.out.println(
            nome + " recebeu: " + mensagem
        );
    }
}
```

```java
import java.util.ArrayList;
import java.util.List;

public class Canal {

    private final List<Observer> inscritos =
            new ArrayList<>();

    public void adicionar(
            Observer observer) {

        inscritos.add(observer);
    }

    public void publicarVideo(
            String titulo) {

        for (Observer observer : inscritos) {

            observer.atualizar(
                "Novo vídeo: " + titulo
            );
        }
    }
}
```

### ▶️ Uso

```java
Canal canal = new Canal();

canal.adicionar(
    new Inscrito("Ana")
);

canal.adicionar(
    new Inscrito("João")
);

canal.publicarVideo(
    "Aprendendo Design Patterns"
);
```

### ☕ Spring

* Eventos
* Listeners
* `ApplicationEventPublisher`
* `@EventListener`

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 3.3 Command

### 🎯 Objetivo

Encapsular requisições em objetos.

### 💻 Exemplo

```java
public interface Command {

    void execute();
}
```

```java
public class SalvarCommand
        implements Command {

    @Override
    public void execute() {

        System.out.println(
            "Salvando..."
        );
    }
}
```

```java
public class ExcluirCommand
        implements Command {

    @Override
    public void execute() {

        System.out.println(
            "Excluindo..."
        );
    }
}
```

### ▶️ Uso

```java
Command command =
        new SalvarCommand();

command.execute();
```

### 📌 Pode permitir

* Undo
* Redo
* Filas
* Histórico de operações
* Agendamento

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 3.4 Chain of Responsibility

### 🎯 Objetivo

Passar uma requisição por vários processadores.

### 💻 Exemplo

```java
public abstract class Handler {

    protected Handler proximo;

    public void definirProximo(
            Handler proximo) {

        this.proximo = proximo;
    }

    public abstract void processar(
            String token);
}
```

```java
public class ValidarToken
        extends Handler {

    @Override
    public void processar(String token) {

        System.out.println(
            "Token validado"
        );

        if (proximo != null) {
            proximo.processar(token);
        }
    }
}
```

```java
public class ValidarPermissao
        extends Handler {

    @Override
    public void processar(String token) {

        System.out.println(
            "Permissão validada"
        );

        if (proximo != null) {
            proximo.processar(token);
        }
    }
}
```

### ▶️ Uso

```java
Handler token =
        new ValidarToken();

Handler permissao =
        new ValidarPermissao();

token.definirProximo(permissao);

token.processar("ABC123");
```

### ☕ Spring Security

O conceito de cadeia aparece na arquitetura de filtros do Spring Security.

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 3.5 State

### 🎯 Objetivo

Alterar o comportamento de um objeto conforme seu estado atual.

### 💻 Exemplo

```java
public interface EstadoPedido {

    void processar();
}
```

```java
public class PedidoNovo
        implements EstadoPedido {

    @Override
    public void processar() {

        System.out.println(
            "Pedido aguardando pagamento"
        );
    }
}
```

```java
public class PedidoPago
        implements EstadoPedido {

    @Override
    public void processar() {

        System.out.println(
            "Pedido pronto para envio"
        );
    }
}
```

```java
public class Pedido {

    private EstadoPedido estado;

    public Pedido() {

        this.estado =
            new PedidoNovo();
    }

    public void setEstado(
            EstadoPedido estado) {

        this.estado = estado;
    }

    public void processar() {

        estado.processar();
    }
}
```

### ▶️ Uso

```java
Pedido pedido =
        new Pedido();

pedido.processar();

pedido.setEstado(
    new PedidoPago()
);

pedido.processar();
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 3.6 Template Method

### 🎯 Objetivo

Definir uma estrutura fixa de algoritmo e permitir que subclasses personalizem determinadas etapas.

### 💻 Exemplo

```java
public abstract class Relatorio {

    public final void gerar() {

        buscarDados();

        formatar();

        exportar();
    }

    private void buscarDados() {

        System.out.println(
            "Buscando dados..."
        );
    }

    protected abstract void formatar();

    private void exportar() {

        System.out.println(
            "Exportando relatório..."
        );
    }
}
```

```java
public class RelatorioPDF
        extends Relatorio {

    @Override
    protected void formatar() {

        System.out.println(
            "Formatando PDF..."
        );
    }
}
```

### ▶️ Uso

```java
Relatorio relatorio =
        new RelatorioPDF();

relatorio.gerar();
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 3.7 Mediator

### 🎯 Objetivo

Centralizar a comunicação entre objetos.

### 💻 Exemplo

```java
public interface ChatMediator {

    void enviar(
        String mensagem,
        Usuario usuario
    );

    void adicionarUsuario(
        Usuario usuario
    );
}
```

```java
public abstract class Usuario {

    protected ChatMediator mediator;
    protected String nome;

    public Usuario(
        ChatMediator mediator,
        String nome
    ) {
        this.mediator = mediator;
        this.nome = nome;
    }

    public abstract void enviar(
        String mensagem
    );

    public abstract void receber(
        String mensagem
    );
}
```

```java
import java.util.ArrayList;
import java.util.List;

public class ChatMediatorImpl
        implements ChatMediator {

    private final List<Usuario> usuarios =
            new ArrayList<>();

    @Override
    public void adicionarUsuario(
            Usuario usuario) {

        usuarios.add(usuario);
    }

    @Override
    public void enviar(
            String mensagem,
            Usuario remetente) {

        for (Usuario usuario : usuarios) {

            if (usuario != remetente) {

                usuario.receber(mensagem);
            }
        }
    }
}
```

O `ChatMediator` centraliza a comunicação.

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 3.8 Memento

### 🎯 Objetivo

Salvar e restaurar estados anteriores de um objeto.

### 💻 Exemplo

```java
public class Memento {

    private final String estado;

    public Memento(String estado) {
        this.estado = estado;
    }

    public String getEstado() {
        return estado;
    }
}
```

```java
public class Editor {

    private String texto;

    public void escrever(String texto) {
        this.texto = texto;
    }

    public Memento salvar() {
        return new Memento(texto);
    }

    public void restaurar(Memento memento) {
        this.texto = memento.getEstado();
    }

    public String getTexto() {
        return texto;
    }
}
```

### ▶️ Uso

```java
Editor editor = new Editor();

editor.escrever("Versão 1");

Memento backup =
        editor.salvar();

editor.escrever("Versão 2");

editor.restaurar(backup);

System.out.println(
    editor.getTexto()
);
```

Resultado:

```text
Versão 1
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 3.9 Iterator

### 🎯 Objetivo

Percorrer coleções sem expor sua estrutura interna.

### 💻 Exemplo Java

```java
import java.util.Iterator;
import java.util.List;

List<String> nomes =
        List.of(
            "Ana",
            "João",
            "Carlos"
        );

Iterator<String> iterator =
        nomes.iterator();

while (iterator.hasNext()) {

    String nome =
            iterator.next();

    System.out.println(nome);
}
```

### Forma simplificada

```java
for (String nome : nomes) {

    System.out.println(nome);
}
```

### ☕ Java

O padrão está presente diretamente no Collections Framework através da interface:

```java
Iterator<T>
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

## 3.10 Visitor

### 🎯 Objetivo

Adicionar novas operações sem alterar as classes existentes.

### 💻 Exemplo

```java
public interface Visitor {

    void visitar(Produto produto);

    void visitar(Servico servico);

    void visitar(Assinatura assinatura);
}
```

```java
public interface Elemento {

    void aceitar(Visitor visitor);
}
```

```java
public class Produto
        implements Elemento {

    @Override
    public void aceitar(
            Visitor visitor) {

        visitor.visitar(this);
    }
}
```

```java
public class Servico
        implements Elemento {

    @Override
    public void aceitar(
            Visitor visitor) {

        visitor.visitar(this);
    }
}
```

```java
public class CalculoImpostoVisitor
        implements Visitor {

    @Override
    public void visitar(
            Produto produto) {

        System.out.println(
            "Calculando imposto do produto"
        );
    }

    @Override
    public void visitar(
            Servico servico) {

        System.out.println(
            "Calculando imposto do serviço"
        );
    }

    @Override
    public void visitar(
            Assinatura assinatura) {

        System.out.println(
            "Calculando imposto da assinatura"
        );
    }
}
```

### ▶️ Uso

```java
Produto produto =
        new Produto();

Visitor visitor =
        new CalculoImpostoVisitor();

produto.aceitar(visitor);
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

# ☕ 4. Quais padrões são mais usados em Java moderno?

| Padrão                      | Onde aparece       |
| --------------------------- | ------------------ |
| **Singleton**               | Beans Spring       |
| **Factory Method**          | Criação de Beans   |
| **Builder**                 | Lombok e DTOs      |
| **Strategy**                | Regras de negócio  |
| **Observer**                | Eventos Spring     |
| **Proxy**                   | AOP e Transações   |
| **Facade**                  | Camada Service     |
| **Decorator**               | Streams e Wrappers |
| **Chain of Responsibility** | Spring Security    |
| **Template Method**         | Frameworks         |

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

# 🧠 5. Mapa Mental Rápido

```text
DESIGN PATTERNS
│
├── 🏗️ CRIACIONAIS
│   ├── Singleton
│   ├── Factory Method
│   ├── Abstract Factory
│   ├── Builder
│   └── Prototype
│
├── 🧱 ESTRUTURAIS
│   ├── Adapter
│   ├── Bridge
│   ├── Composite
│   ├── Decorator
│   ├── Facade
│   ├── Flyweight
│   └── Proxy
│
└── 🔄 COMPORTAMENTAIS
    ├── Strategy
    ├── Observer
    ├── Command
    ├── Chain of Responsibility
    ├── State
    ├── Template Method
    ├── Mediator
    ├── Memento
    ├── Iterator
    └── Visitor
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

# 📊 6. Resumo dos Padrões

|  # | Padrão                  | Categoria      | Palavra-chave           |
| -: | ----------------------- | -------------- | ----------------------- |
|  1 | Singleton               | Criacional     | Uma instância           |
|  2 | Factory Method          | Criacional     | Criar                   |
|  3 | Abstract Factory        | Criacional     | Família de objetos      |
|  4 | Builder                 | Criacional     | Construção              |
|  5 | Prototype               | Criacional     | Clonagem                |
|  6 | Adapter                 | Estrutural     | Compatibilidade         |
|  7 | Bridge                  | Estrutural     | Separação               |
|  8 | Composite               | Estrutural     | Hierarquia              |
|  9 | Decorator               | Estrutural     | Adicionar comportamento |
| 10 | Facade                  | Estrutural     | Simplificar             |
| 11 | Flyweight               | Estrutural     | Compartilhar            |
| 12 | Proxy                   | Estrutural     | Controlar acesso        |
| 13 | Strategy                | Comportamental | Algoritmo               |
| 14 | Observer                | Comportamental | Notificação             |
| 15 | Command                 | Comportamental | Requisição              |
| 16 | Chain of Responsibility | Comportamental | Pipeline                |
| 17 | State                   | Comportamental | Estado                  |
| 18 | Template Method         | Comportamental | Algoritmo               |
| 19 | Mediator                | Comportamental | Comunicação             |
| 20 | Memento                 | Comportamental | Restaurar estado        |
| 21 | Iterator                | Comportamental | Percorrer               |
| 22 | Visitor                 | Comportamental | Operações               |

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

# 🎯 7. Como Memorizar

Associe cada padrão ao **problema que ele resolve**:

```text
Uma única instância?
        ↓
    Singleton

Decidir qual objeto criar?
        ↓
    Factory

Criar uma família de objetos?
        ↓
 Abstract Factory

Objeto com muitos parâmetros?
        ↓
     Builder

Copiar um objeto existente?
        ↓
    Prototype

Interfaces incompatíveis?
        ↓
     Adapter

Abstração e implementação variam separadamente?
        ↓
      Bridge

Estruturas hierárquicas?
        ↓
    Composite

Adicionar comportamento?
        ↓
    Decorator

Sistema complexo com interface simples?
        ↓
      Facade

Economizar memória compartilhando objetos?
        ↓
    Flyweight

Controlar acesso a um objeto?
        ↓
      Proxy

Vários algoritmos possíveis?
        ↓
     Strategy

Notificar vários interessados?
        ↓
     Observer

Transformar uma ação em objeto?
        ↓
     Command

Passar uma requisição por várias etapas?
        ↓
Chain of Responsibility

Comportamento depende do estado?
        ↓
      State

Algoritmo com etapas fixas?
        ↓
 Template Method

Muitos objetos precisam se comunicar?
        ↓
     Mediator

Salvar/restaurar um estado?
        ↓
     Memento

Percorrer uma coleção?
        ↓
     Iterator

Adicionar operações a uma estrutura?
        ↓
     Visitor
```

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)

---

# 💼 8. Para Entrevistas Java/Spring Boot

Os padrões que vale especialmente saber **explicar e implementar na prática** são:

```text
                    DESIGN PATTERNS
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
   🏗️ Criacionais      🧱 Estruturais     🔄 Comportamentais
        │                  │                  │
        ├─ Factory         ├─ Adapter        ├─ Strategy ⭐
        ├─ Builder ⭐      ├─ Decorator      ├─ Observer
        └─ Singleton       ├─ Facade ⭐      ├─ State
                           └─ Proxy ⭐       └─ Chain ⭐
```

### 🔥 Especial atenção em Spring Boot

| Padrão                      | Importância prática                      |
| --------------------------- | ---------------------------------------- |
| **Strategy**                | Regras de negócio                        |
| **Factory**                 | Criação e seleção de implementações      |
| **Builder**                 | DTOs e objetos complexos                 |
| **Adapter**                 | Integração com sistemas externos/legados |
| **Facade**                  | Organização de serviços                  |
| **Proxy**                   | AOP, transações e segurança              |
| **Chain of Responsibility** | Filtros e segurança                      |
| **Observer**                | Eventos assíncronos                      |
| **Singleton**               | Gerenciamento de Beans                   |

> **Regra principal:** primeiro identifique o problema; depois escolha o Design Pattern. Não aplique um padrão simplesmente porque ele existe.

---

[⬆️ **Voltar ao Índice**](#-índice-navegável)
