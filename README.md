# PhotoMorph - Geração de Imagens a partir de um Banco de Dados

PhotoMorph é uma aplicação Java que recebe uma imagem de referência e, a partir de um banco de imagens, gera uma nova imagem que tenta replicar a referência. O projeto utiliza técnicas de processamento de imagens, como cálculo de cores médias e distância euclidiana, para encontrar as imagens mais adequadas do banco de dados e combiná-las em uma única imagem final.


## Exemplo: 
[Imagem Referência](src/example/reference.jpg)
[Imagem Gerada](src/example/result_image.jpg)

## Funcionalidades
- Divisão da Imagem de Referência: A imagem de referência é dividida em blocos menores para processamento.

- Cálculo de Cores Médias: Calcula a cor média de cada bloco da imagem de referência e das imagens do banco de dados.

- Mapeamento de Imagens: Encontra a imagem do banco de dados mais próxima em termos de cor para cada bloco da imagem de referência.

- Geração de Imagem Final: Combina as imagens selecionadas do banco de dados para criar uma nova imagem que se assemelha à referência.

- Processamento Paralelo: Utiliza threads para acelerar o processamento de imagens.

## Tecnologias Utilizadas

- Java 21: Linguagem de programação principal.

- Maven: Gerenciamento de dependências e build do projeto.

- imgscalr: Biblioteca para redimensionamento de imagens com alta qualidade.

- Concorrência: Uso de ExecutorService e CompletableFuture para processamento paralelo.

## Estrutura 

```
photomorph/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── imageprocess/
│   │   │       └── ImageProcess.java (Código principal)
│   │   └── resources/
│   │       └── assets/
│   │           ├── image/
│   │           │   ├── reference.jpg (Imagem de referência)
│   │           │   ├── dataset/ (Pasta com imagens do banco de dados)
│   │           │   └── result/ (Pasta para salvar a imagem gerada)
│   └── test/
│       └── java/
│           └── imageprocess/
│               └── ImageProcessTest.java (Testes unitários)
├── pom.xml (Configuração do Maven)
└── README.md (Documentação do projeto)
```

## ⚙️ Configuração do Projeto

1. Clone o repositório 
  ```bash
  git clone https://github.com/DsGrilo/photomorph.git
  cd photomorph
  ```
2. Instale as dependências
   ```bash
   mvn clean install
  
3. Prepare as imagens:
- Coloque a imagem de referência em src/main/resources/assets/image/reference.jpg.

- Adicione as imagens do banco de dados na pasta src/main/resources/assets/image/dataset.
    - Afins de teste usei o [Kaggle](https://www.kaggle.com/datasets/tunguz/1-million-fake-faces?resource=download "Ir para o Kaggle") usando 30 Mil imagens
4. Execute a aplicação:
```bash
  mvn exec:java -Dexec.mainClass="imageprocess.ImageProcess"
```
5. Resultado
   - A imagem gerada será salva em src/main/resources/assets/image/result/result_image.jpg.
## 🧠 Como Funciona
-Divisão da Imagem de Referência:
  - A imagem de referência é dividida em blocos de tamanho definido (BLOCKSIZE).

- Cálculo de Cores Médias:
  - Para cada bloco da imagem de referência e para cada imagem do banco de dados, a cor média é calculada.

- Mapeamento de Imagens:
  - Para cada bloco da imagem de referência, a imagem do banco de dados com a cor média mais próxima é selecionada.

- Geração da Imagem Final:
  - As imagens selecionadas são redimensionadas e combinadas para criar a imagem final.

- Processamento Paralelo:
  - O uso de threads acelera o processamento, especialmente para bancos de dados grandes.

## 📦 Dependências
O projeto utiliza a biblioteca imgscalr para redimensionamento de imagens. A dependência é adicionada no pom.xml:
```xml
<dependencies>
    <dependency>
        <groupId>org.imgscalr</groupId>
        <artifactId>imgscalr-lib</artifactId>
        <version>4.2</version>
    </dependency>
</dependencies>
```

##  🚧 Projeto em andamento
- Ainda estou trabalhando no código então o código pode ser alterado a qualquer momento
- Possiveis melhorias que farei
  - Uso de Outras Métricas de Similaridade:
  - Redimensionamento mais Inteligente
    

## 🤝 Contribuição
- Contribuições são bem-vindas! Sinta-se à vontade para abrir issues ou enviar pull requests para melhorar o projeto.
