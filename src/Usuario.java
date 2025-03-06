public class Usuario {
    private String login;
    private String senha;

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
}
