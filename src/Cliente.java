import java.io.*;
import java.net.*;
import java.util.*;
import java.nio.file.*;

public class Cliente {
    public static void main(String[] args) {
        int port = 8080, opcaoMenu;
        String usuarioCadastro, usuarioLogin, senhaCadastro, senhaConfirmacao, senhaLogin;
        List<Usuario> cadastros = new ArrayList();
        boolean desligado = false, loginConfirmado = false;
        Scanner sc = new Scanner(System.in);
        Usuario usuarioLogado = null;

        //debug
        cadastros.add(new Usuario("admin", "admin"));

        //Menu
        do{
            System.out.println("Olá, bem-vindo ao MegaDrive.");
            System.out.println("-----------------------------");
            System.out.println("O que você deseja fazer?");
            System.out.println("1. Login");
            System.out.println("2. Cadastrar novo usuario");
            System.out.println("3. Sair");
            System.out.print("Digite sua opcao: ");
            opcaoMenu = sc.nextInt();
            sc.nextLine();

            switch (opcaoMenu) {
                case 1:
                    boolean senhaCorreta = false;
                    //Login
                    System.out.print("Digite seu usuario: ");
                    usuarioLogin = sc.nextLine();

                    for (Usuario usuario : cadastros) {
                        if (usuarioLogin.equals(usuario.getLogin())) {
                            usuarioLogado = usuario;
                            break;
                        }
                    }

                    if (usuarioLogado == null) {
                        System.out.println("Usuário não encontrado!");
                        break;
                    }

                    do {
                        System.out.print("Digite sua senha: ");
                        senhaLogin = sc.nextLine();

                        if (senhaLogin.equals(usuarioLogado.getSenha())) {
                            System.out.println("Usuário logado com sucesso!");
                            loginConfirmado = true;
                            senhaCorreta = true;
                        } else {
                            System.out.println("Senha incorreta! Tente novamente.");
                        }
                    } while (!senhaCorreta);
                    break;

                case 2:
                    //Cadastro
                    boolean senhaValida = true, usuarioValido = true, usuarioExistente = false;

                    do{
                        System.out.print("Digite o nome do novo usuario: ");
                        usuarioCadastro = sc.nextLine();

                        for (Usuario u : cadastros) {
                            if(u.getLogin().equals(usuarioCadastro)) {
                                usuarioExistente = true;
                            } else {
                                usuarioExistente = false;
                            }
                        }
                        if(usuarioCadastro.isEmpty() || usuarioExistente) {
                            System.out.println("Esse usuário já existe ou está vazio");
                            usuarioValido = false;
                        } else {
                            usuarioValido = true;
                            do {
                                System.out.print("Digite a senha do novo usuario: ");
                                senhaCadastro = sc.nextLine();
                                System.out.print("Digite novamente a senha do novo usuario: ");
                                senhaConfirmacao = sc.nextLine();
                                if(!senhaConfirmacao.equals(senhaCadastro) || senhaCadastro.isEmpty()){
                                    System.out.println("Senha incorreta ou vazia.");
                                    senhaValida = false;
                                } else {
                                    senhaValida = true;
                                    Usuario novoUsuario = new Usuario(usuarioCadastro, senhaCadastro);
                                    cadastros.add(novoUsuario);
                                }
                            }while (!senhaValida);
                        }
                    } while (!usuarioValido);
                    System.out.println("Usuário cadastrado com sucesso!");
                    break;
                case 3:
                    desligado = true;
                    break;
                default:
                    System.out.println("Opção Inválida.");
            }
        }while (!desligado && !loginConfirmado);

        //Conexão com o Servidor
        if(loginConfirmado){
            boolean logado = true;
            int clienteOpcao;
            try (Socket socket = new Socket("localhost", port)) {
                System.out.println("Conectado ao servidor!");
                System.out.println("Bem-vindo! " + usuarioLogado.getLogin());
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(usuarioLogado.getLogin());

                while(logado) {
                    System.out.println("-----------------------------");
                    System.out.println("Escolha uma opção abaixo:");
                    System.out.println("1. Enviar arquivo");
                    System.out.println("2. Baixar arquivo");
                    System.out.println("3. Atualizar diretório");
                    System.out.println("4. Sair");
                    System.out.print("Digite sua opcao: ");
                    clienteOpcao = sc.nextInt();
                    sc.nextLine();

                    switch (clienteOpcao) {
                        case 1:
                            enviarArquivo(dos, clienteOpcao, sc);
                            break;
                        case 2:
                            baixarArquivo(dis, dos, clienteOpcao, sc, usuarioLogado);
                            break;
                        case 3:
                            escolherDiretorioParaDownload(sc, usuarioLogado);
                            break;
                        case 4:
                            logado = false;
                            dos.close();
                            sc.close();
                            break;
                        default:
                            System.out.println("Escolha uma opção válida!");
                    }
                }
            } catch (IOException e) {
                System.err.println("Erro no cliente: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void enviarArquivo(DataOutputStream dos, int clienteOpcao, Scanner sc){
        try{
            dos.writeInt(clienteOpcao);
            System.out.print("Digite o diretório do Arquivo que você deseja enviar:");
            String arquivo = sc.nextLine();

            File file = new File(arquivo);
            FileInputStream fis = new FileInputStream(file);

            dos.writeUTF(file.getName());

            System.out.println("Enviando arquivo: " + file.getName());
            int byteData;
            while ((byteData = fis.read()) != -1) {
                dos.write(byteData);
            }

            System.out.println("Arquivo enviado com sucesso!");
            return;
        } catch (IOException ex) {
            System.err.println("Erro no cliente: " + ex.getMessage());
        }
    }

    public static void baixarArquivo(DataInputStream dis, DataOutputStream dos, int clienteOpcao, Scanner sc, Usuario usuarioLogado){
        try{
            dos.writeInt(clienteOpcao);
            int tipoArquivoOpcao, numeroArquivo = 1, downloadOpcao;
            String tipoArquivo = null;
            System.out.println("-----------------------------");
            System.out.println("Selecione qual tipo de arquivo deseja baixar: ");
            System.out.println("1. Documentos");
            System.out.println("2. Imagens");
            System.out.println("3. Videos");
            System.out.println("4. Audios");
            System.out.print("Digite sua opcao: ");
            tipoArquivoOpcao = sc.nextInt();
            sc.nextLine();

            switch (tipoArquivoOpcao) {
                case 1:
                    tipoArquivo = "docs";
                    break;
                case 2:
                    tipoArquivo = "images";
                    break;
                case 3:
                    tipoArquivo = "videos";
                    break;
                case 4:
                    tipoArquivo = "audios";
                    break;
                default:
                    System.out.println("Opção Inválida.");
            }

            File diretorio = new File("./drive/" +  usuarioLogado.getLogin() + "/" + tipoArquivo);
            File[] arquivos = diretorio.listFiles();
            String nomeArquivo = null;

            if(arquivos == null || arquivos.length == 0){
                System.out.println("O drive está vazio.");
                return;
            } else {
                for (File f : arquivos) {
                    if(f.isFile()){
                        System.out.println(numeroArquivo + ". " + f.getName());
                        numeroArquivo++;
                        nomeArquivo = f.getName();
                    }
                }
            }
            System.out.println("Selecione o número do arquivo que deseja baixar: ");
            downloadOpcao = sc.nextInt() - 1;
            sc.nextLine();
            String diretorioArquivo = arquivos[downloadOpcao].toString();
            System.out.println(nomeArquivo);
            if(Files.exists(Paths.get(usuarioLogado.getUserDiretorio() + nomeArquivo))){
                System.out.println("Esse arquivo já existe nesse diretório");
                return;
            }
            dos.writeUTF(diretorioArquivo);
            System.out.println(diretorioArquivo);

            long tamanhoArquivo = dis.readLong();
            File arquivoRecebido = new File(usuarioLogado.getUserDiretorio() + nomeArquivo);
            try (FileOutputStream fos = new FileOutputStream(arquivoRecebido)) {
                for (long i = 0; i < tamanhoArquivo; i++) {
                    fos.write(dis.read());
                }
            }
            System.out.println("Arquivo recebido com sucesso!");
        } catch (IOException e){
            System.err.println("Erro no cliente: " + e.getMessage());
        }
    }

    public static void escolherDiretorioParaDownload(Scanner sc, Usuario usuarioLogado){
        String novoDiretorio = null;
        System.out.println("Diretório atual:" + usuarioLogado.getUserDiretorio());
        System.out.println("Escolha um diretório para salvar os arquivos baixados: ");
        novoDiretorio = sc.nextLine();
        usuarioLogado.setUserDiretorio(novoDiretorio);
        System.out.println("Diretório atualizado com sucesso!");
    }
}