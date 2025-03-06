import java.io.*;
import java.net.*;
import java.nio.file.*;

public class Servidor {
    public static void main(String[] args) {
        int port = 8080;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Servidor aguardando conexão...");

            try (Socket socket = serverSocket.accept()) {
                DataInputStream dis = new DataInputStream(socket.getInputStream());
                DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                String usuarioLogado = dis.readUTF();

                System.out.println("Cliente conectado: " + usuarioLogado);

                try {
                    Files.createDirectories(Paths.get("./drive/" + usuarioLogado));
                    Files.createDirectories(Paths.get("./drive/" + usuarioLogado + "/images"));
                    Files.createDirectories(Paths.get("./drive/" + usuarioLogado + "/videos"));
                    Files.createDirectories(Paths.get("./drive/" + usuarioLogado + "/docs"));
                    Files.createDirectories(Paths.get("./drive/" + usuarioLogado + "/audios"));
                    System.out.println("Diretórios criados!");
                } catch (Exception e) {
                    System.out.println("Erro ao criar o diretório: " + e.getMessage());
                }

                int opcao = dis.readInt();

                if(opcao == 1) {
                    receberArquivo(dis, usuarioLogado);
                } else if (opcao == 2) {
                    enviarArquivo(dos, dis);
                } else {

                }
            }
        } catch (IOException e) {
            System.err.println("Erro no servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void receberArquivo(DataInputStream dis, String usuarioLogado) {
        try{
            String nomeDoArquivo = dis.readUTF();
            String tipoArquivo = null;
            if(nomeDoArquivo.endsWith(".jpg") || nomeDoArquivo.endsWith(".png") || nomeDoArquivo.endsWith(".gif")) {
                tipoArquivo = "images";
            } else if (nomeDoArquivo.endsWith(".mp4") || nomeDoArquivo.endsWith(".avi")) {
                tipoArquivo = "videos";
            } else if (nomeDoArquivo.endsWith(".mp3")) {
                tipoArquivo = "audios";
            } else if (nomeDoArquivo.endsWith(".pdf") || nomeDoArquivo.endsWith(".doc")) {
                tipoArquivo = "docs";
            }
            FileOutputStream fos = new FileOutputStream("drive/" + usuarioLogado + "/" + tipoArquivo + "/" + nomeDoArquivo);
            int byteData;
            while ((byteData = dis.read()) != -1) {
                fos.write(byteData);
            }
            System.out.println("Arquivo recebido com sucesso!");
        } catch (IOException e){
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }

    public static void enviarArquivo(DataOutputStream dos, DataInputStream dis) {
        try{
            String caminhoArquivoDrive = dis.readUTF();
            File arquivoParaEnviar = new File(caminhoArquivoDrive);
            FileInputStream fis = new FileInputStream(arquivoParaEnviar);
            long tamanhoArquivo = arquivoParaEnviar.length();

            dos.writeLong(tamanhoArquivo);

            System.out.println("Baixando o arquivo: " + arquivoParaEnviar.getName());
            int byteData;
            while ((byteData = fis.read()) != -1) {
                dos.write(byteData);
            }
            System.out.println("Arquivo enviado com sucesso!");
        } catch (IOException e){
            System.err.println("Erro no servidor: " + e.getMessage());
        }
    }
}
