public class Usuario {
    private String login;
    private String senha;
    private String userDiretorio = "C:\\Users\\Public\\Downloads\\";

    public Usuario(String _login, String _senha) {
        this.login = _login;
        this.senha = _senha;
    }

    public String getLogin() {
        return login;
    }

    public String getSenha() {
        return senha;
    }

    public String getUserDiretorio() {
        return userDiretorio;
    }

    public void setUserDiretorio(String userDiretorio) {
        this.userDiretorio = userDiretorio;
    }
}
