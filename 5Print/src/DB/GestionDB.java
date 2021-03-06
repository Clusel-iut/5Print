package DB;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;

import DTO.Adresse;
import DTO.Agenda;
import DTO.Album;
import DTO.BonAchat;
import DTO.Cadre;
import DTO.Calendrier;
import DTO.Client;
import DTO.Commande;
import DTO.FichierPhoto;
import DTO.Impression;
import DTO.Photo;
import DTO.PointRelais;
import DTO.StatutCommande;
import DTO.Stock;
import DTO.Tirage;
import DTO.TypeSupport;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GestionDB {

	public static final String CONN_URL = "jdbc:oracle:thin:@im2ag-oracle.e.ujf-grenoble.fr:1521:im2ag";
	public static Connection conn;

	public static void configure(String user, String passwd) {
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			conn = DriverManager.getConnection(CONN_URL, user, passwd);
			conn.setAutoCommit(false);
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
	}

	public static void configure() {
		try {
			DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
			conn = DriverManager.getConnection(CONN_URL, "ouzzineo", "Oussama123");
			conn.setAutoCommit(false);
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

	}

	//
	// ADRESSE
	//
	// GET
	public static Adresse getAdresseById(int id) {
		String sql = "SELECT * FROM ADRESSE WHERE ID_ADRESSE = ?";
		Adresse adr = null;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, id);
			ResultSet result = statement.executeQuery();

			Client client = null;
			if (result.next()) {
				adr = new Adresse(result.getInt("ID_ADRESSE"), result.getString("PAYS"), result.getString("VILLE"),
						result.getString("CODE_POSTAL"), result.getString("RUE"), result.getString("NUMERO"), client);
				result.close();
				statement.close();
			}
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return adr;
	}

	// CREATE
	public static boolean createAdresse(String pays, String email, String ville, String code_postal, String rue,
			String numero) {
		String sql = "INSERT INTO ADRESSE (PAYS, EMAIL, VILLE, CODE_POSTAL, RUE, NUMERO) VALUES (?,?,?,?,?,?)";
		boolean isAdded = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, pays);
			statement.setString(2, email);
			statement.setString(3, ville);
			statement.setString(4, code_postal);
			statement.setString(5, rue);
			statement.setString(6, numero);

			int rowsInserted = statement.executeUpdate();
			statement.close();
			if (rowsInserted > 0) {
				isAdded = true;
			}
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isAdded = false;
		}

		return isAdded;
	}

	// UPDATE
	public static boolean updateAdresse(Adresse adresse) {
		String sql = "UPDATE ADRESSE SET PAYS = ?, VILLE = ?, CODE_POSTAL = ?, RUE = ?, NUMERO = ? WHERE ID_ADRESSE = ?";
		boolean isUpdated = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, adresse.getPays());
			statement.setString(2, adresse.getVille());
			statement.setString(3, adresse.getCode_postal());
			statement.setString(4, adresse.getRue());
			statement.setString(5, adresse.getNumero());
			statement.setInt(6, adresse.getId_adresse());

			int rowsInserted = statement.executeUpdate();

			if (rowsInserted > 0) {
				isUpdated = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isUpdated = false;
		}

		return isUpdated;
	}

	// DELETE
	public static boolean deleteAdresseById(int id) {
		String sql = "DELETE FROM ADRESSE WHERE ID_ADRESSE = ?";
		boolean isDeleted = false;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, id);

			int rowsDeleted = statement.executeUpdate();
			statement.close();
			if (rowsDeleted > 0) {
				isDeleted = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isDeleted = false;
		}
		return isDeleted;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////

	//
	// Client
	//
	// GET
	public static Client getClientByEmail(String email) {
		String sql = "SELECT * FROM CLIENT WHERE EMAIL = ?";
		Client cli = null;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, email);
			ResultSet result = statement.executeQuery();

			if (result.next()) {
				cli = new Client(result.getString("EMAIL"), result.getString("NOM"), result.getString("PRENOM"),
						getAllAdresseByClientId(cli, email), result.getString("MOT_DE_PASSE"),
						getAllPhotosPartageesByClientId(cli, email), getAllPhotosByClientId(cli, email),
						getAllImpressionsByClientId(cli, email), getAllCommandesByClientId(cli, email));
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return cli;
	}

	private static Client getSimpleClientByEmail(String email) {
		String sql = "SELECT * FROM CLIENT WHERE EMAIL = ?";
		Client cli = null;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, email);
			ResultSet result = statement.executeQuery();

			if (result.next()) {
				cli = new Client(result.getString("EMAIL"), result.getString("NOM"), result.getString("PRENOM"),
						result.getString("MOT_DE_PASSE"));
				statement.close();
				conn.commit();
			}
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return cli;
	}

	public static ArrayList<Client> getAllClients() {
		ArrayList<Client> clients = new ArrayList<Client>();
		String sql = "SELECT * FROM CLIENT";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				clients.add(new Client(result.getString("EMAIL"), result.getString("NOM"), result.getString("PRENOM"),
						result.getString("MOT_DE_PASSE")));
			}
			statement.close();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

		return clients;
	}

	public static ArrayList<Commande> getAllCommandes() {
		ArrayList<Commande> commandes = new ArrayList<Commande>();
		String sql = "SELECT * FROM COMMANDE";
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				ArrayList<Impression> imps = null; // result.getString("EMAIL") OK

				BonAchat bon_achat = getBonAchatById(result.getString("CODE_BON")); // result.getString("CODE_BON")
				BonAchat bon_gen = getBonAchatById(result.getString("CODE_BON_GENERE")); // result.getString("CODE_BON_GENERE")
				Adresse ad = getAdresseById(result.getInt("ID_ADRESSE"));
				Client clt = getClientSansPhotosByEmail(result.getString("EMAIL"));
				Date dtLivr = result.getDate("DATE_COMMANDE");

				Commande c = new Commande(result.getInt("NUMERO"), bon_achat, bon_gen, ad, clt, imps,
						result.getString("MODE_LIVRAISON"), dtLivr, StatutCommande.valueOf(result.getString("STATUT")),
						result.getBoolean("ETAT_PAIEMENT"), result.getFloat("MONTANT_TOTAL_CMD"));
				if (c != null) {
					if (bon_achat != null) {
						bon_achat.setCommande(c);
					}
					if (bon_gen != null) {
						bon_gen.setCommandeGeneree(c);
					}
					commandes.add(c);
				}

			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

		return commandes;
	}

	private static ArrayList<Commande> getAllCommandesByClientId(Client cli, String email) {
		ArrayList<Commande> commandes = new ArrayList<Commande>();
		String sql = "SELECT NUMERO FROM COMMANDE WHERE EMAIL = ?";
		PreparedStatement statement;
		Commande commande;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, email);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				commande = getCommandeById(result.getInt("NUMERO"));
				if (commande != null) {
					commande.setClient(cli);
					if (commande.getBon_achat() != null) {
						commande.getBon_achat().setClient(cli);
					}
					commandes.add(commande);
				}
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

		return commandes;
	}

	private static <T extends Impression> ArrayList<Impression> getAllImpressionsByClientId(Client cli, String email) {
		ArrayList<Impression> impressions = new ArrayList<Impression>();
		String sql = "SELECT ID_IMPRESSION FROM IMPRESSION WHERE EMAIL = ?";
		PreparedStatement statement;
		T impression;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, email);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				impression = getImpressionById(result.getInt("ID_IMPRESSION"));
				if (impression != null) {
					impression.setClient(cli);
					impressions.add(impression);
				}
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

		return impressions;
	}

	private static ArrayList<FichierPhoto> getAllPhotosPartageesByClientId(Client cli, String email) {
		ArrayList<FichierPhoto> photos = new ArrayList<FichierPhoto>();
		String sql = "SELECT CHEMIN FROM PARTAGE WHERE EMAIL = ?";
		PreparedStatement statement;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, email);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				FichierPhoto fp = getFichierPhotoById(false, result.getString("CHEMIN"));
				String sqlC = "SELECT EMAIL FROM FICHIERPHOTO WHERE CHEMIN = ?";
				PreparedStatement statement2;
				statement2 = conn.prepareStatement(sqlC);
				statement2.setString(1, result.getString("CHEMIN"));
				ResultSet resultC = statement2.executeQuery();
				if (resultC.next()) {
					Client client = getClientSansPhotosByEmail(resultC.getString("EMAIL"));
					fp.setClient(client);
					photos.add(fp);
				}
				statement2.close();
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

		return photos;
	}

	private static Client getClientSansPhotosByEmail(String email) {
		String sql = "SELECT * FROM CLIENT WHERE EMAIL = ?";
		Client cli = null;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, email);
			ResultSet result = statement.executeQuery();

			if (result.next()) {
				cli = new Client(result.getString("EMAIL"), result.getString("NOM"), result.getString("PRENOM"), null,
						result.getString("MOT_DE_PASSE"), null, null, null, null);
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return cli;
	}

	private static ArrayList<FichierPhoto> getAllPhotosByClientId(Client cli, String email) {
		ArrayList<FichierPhoto> photos = new ArrayList<FichierPhoto>();
		String sql = "SELECT CHEMIN FROM FICHIERPHOTO WHERE EMAIL = ?";
		PreparedStatement statement;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, email);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				FichierPhoto fp = getFichierPhotoById(false, result.getString("CHEMIN"));
				fp.setClient(cli);
				photos.add(fp);
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

		return photos;
	}

	private static ArrayList<Adresse> getAllAdresseByClientId(Client cli, String email) {
		ArrayList<Adresse> adresses = new ArrayList<Adresse>();
		String sql = "SELECT ID_ADRESSE FROM ADRESSE WHERE EMAIL = ?";
		PreparedStatement statement;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, email);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				Adresse adresse = getAdresseById(result.getInt("ID_ADRESSE"));
				adresse.setClient(cli);
				adresses.add(adresse);
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

		return adresses;
	}

	// CREATE
	public static boolean createClient(String email, String nom, String prenom, String motdepasse) {
		String sql = "INSERT INTO CLIENT (EMAIL, NOM, PRENOM, MOT_DE_PASSE) VALUES (?,?,?,?)";
		boolean isAdded = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, email);
			statement.setString(2, nom);
			statement.setString(3, prenom);
			statement.setString(4, motdepasse);

			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				isAdded = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isAdded = false;
		}

		return isAdded;
	}

	public static boolean createClient(Client client) {
		boolean isAddedC = createClient(client.getEmail(), client.getNom(), client.getPrenom(), client.getMotDePasse());
		for (Adresse a : client.getAdresse()) {
			createAdresse(a.getPays(), client.getEmail(), a.getVille(), a.getCode_postal(), a.getRue(), a.getNumero());
		}
		return isAddedC;
	}

	// UPDATE
	public static boolean updateClient(Client client) {
		String sql = "UPDATE CLIENT SET NOM = ?, PRENOM = ?, MOT_DE_PASSE = ? WHERE EMAIL = ?";
		boolean isUpdated = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, client.getNom());
			statement.setString(2, client.getPrenom());
			statement.setString(3, client.getMotDePasse());
			statement.setString(4, client.getEmail());

			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				isUpdated = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isUpdated = false;
		}

		return isUpdated;
	}

	// DELETE
	public static boolean deleteClient(Client client) {
		String sql = "DELETE CLIENT WHERE EMAIL = ?";
		boolean isDeleted = false;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			for (Commande c : client.getCommandes()) {
				deleteCommande(c);
			}
			for (FichierPhoto fp : client.getPhotos()) {
				deleteFichierPhoto(fp.getChemin());
			}
			for (Adresse ad : client.getAdresse()) {
				deleteAdresseById(ad.getId_adresse());
			}
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, client.getEmail());

			int rowsDeleted = statement.executeUpdate();
			if (rowsDeleted > 0) {
				isDeleted = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isDeleted = false;
		}
		return isDeleted;
	}

	public static boolean connectionClient(String email, String motDePasse) {
		String sql = "SELECT * FROM CLIENT WHERE EMAIL = ? AND MOT_DE_PASSE = ?";
		boolean isConnected = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, email);
			statement.setString(2, motDePasse);

			ResultSet result = statement.executeQuery();
			if (result.next()) {
				isConnected = true;

			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isConnected = false;

		}

		if (isConnected) {
			String sqlU = "UPDATE CLIENT SET DATE_CONNECT = ? WHERE EMAIL = ?";
			PreparedStatement statementU;
			try {
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				statementU = conn.prepareStatement(sqlU);
				statementU.setDate(1, new java.sql.Date(System.currentTimeMillis()));
				statementU.setString(2, email);
				statementU.executeUpdate();
				statementU.close();
				conn.commit();
			} catch (SQLException e) { popupException(e);
			}
		}

		return isConnected;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////

	//
	// Commande
	//
	// GET
	public static Commande getCommandeById(int id) {
		String sql = "SELECT * FROM COMMANDE WHERE NUMERO = ?";
		Commande cmd = null;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			
			//update montant
			PreparedStatement stmt = conn.prepareStatement("SELECT CALCUL_MONTANT_COMMANDE(?) FROM DUAL");
			stmt.setInt(1,id);       
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				  int output = rs.getInt(1);
			}
			
			stmt.close();
			
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, id);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				BonAchat bon_achat = null;
				BonAchat bon_achat_genere = null;
				Client client = null;
				Adresse adresse = getAdresseById(result.getInt("ID_ADRESSE"));

				if (result.getString("CODE_BON") != null) {
					bon_achat = getBonAchatById(result.getString("CODE_BON"));
					bon_achat.setCommande(cmd);
				}
				if (result.getString("CODE_BON_GENERE") != null) {
					bon_achat = getBonAchatById(result.getString("CODE_BON_GENERE"));
					bon_achat.setCommandeGeneree(cmd);
				}

				cmd = new Commande(result.getInt("NUMERO"), bon_achat, bon_achat_genere, adresse, client,
						getAllImpressionByCommandeId(cmd, result.getInt("NUMERO")), result.getString("MODE_LIVRAISON"),
						result.getDate("DATE_COMMANDE"), StatutCommande.valueOf(result.getString("STATUT")),
						result.getBoolean("ETAT_PAIEMENT"), result.getFloat("MONTANT_TOTAL_CMD"));

			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return cmd;
	}

	static ArrayList<Impression> getAllImpressionByCommandeId(Commande commande, int numero) {
		ArrayList<Impression> impressions = new ArrayList<Impression>();
		String sql = "SELECT ID_IMPRESSION FROM IMPRESSION WHERE NUMERO = ?";
		PreparedStatement statement;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setInt(1, numero);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				Impression imp = getImpressionById(result.getInt("ID_IMPRESSION"));
				System.out.println(result.getInt("ID_IMPRESSION") + " " + imp.toString());
				imp.setCommande(commande);
				impressions.add(imp);
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

		return impressions;
	}

	// CREATE
	public static int createCommande(int adresse, String email, String mode_livraison, StatutCommande statut,
			boolean etat_paiement, float montant_total_cmd) {
		int id_commande = -1;
		try {
			String sql;
			PreparedStatement statement;
			if (adresse == 0) {
				sql = "INSERT INTO COMMANDE (ID_ADRESSE, EMAIL, MODE_LIVRAISON, DATE_COMMANDE, STATUT, ETAT_PAIEMENT, MONTANT_TOTAL_CMD) "
						+ "VALUES (NULL,?,?,?,?,?,?)";
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				statement = conn.prepareStatement(sql);
				statement.setString(1, email);
				statement.setString(2, mode_livraison);
				LocalDate todayLocalDate = LocalDate.now(ZoneId.systemDefault());
				java.sql.Date sqlDate = java.sql.Date.valueOf(todayLocalDate);
				statement.setDate(3, sqlDate);
				statement.setString(4, statut.toString());
				statement.setBoolean(5, etat_paiement);
				statement.setFloat(6, montant_total_cmd);
			} else {
				sql = "INSERT INTO COMMANDE (ID_ADRESSE, EMAIL, MODE_LIVRAISON, DATE_COMMANDE, STATUT, ETAT_PAIEMENT, MONTANT_TOTAL_CMD) "
						+ "VALUES (?,?,?,?,?,?,?)";
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				statement = conn.prepareStatement(sql);
				statement.setInt(1, adresse);
				statement.setString(2, email);
				statement.setString(3, mode_livraison);
				LocalDate todayLocalDate = LocalDate.now(ZoneId.systemDefault());
				java.sql.Date sqlDate = java.sql.Date.valueOf(todayLocalDate);
				statement.setDate(4, sqlDate);
				statement.setString(5, statut.toString());
				statement.setBoolean(6, etat_paiement);
				statement.setFloat(7, montant_total_cmd);
			}

			int rowsInserted = statement.executeUpdate();

			PreparedStatement statementID = conn
					.prepareStatement("SELECT MAX(NUMERO) AS ID FROM COMMANDE WHERE EMAIL = ?");
			statementID.setString(1, email);
			ResultSet resultImp = statementID.executeQuery();

			if (resultImp.next() && rowsInserted > 0) {
				id_commande = resultImp.getInt("ID");
			}
			statementID.close();
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			id_commande = -1;
		}

		return id_commande;
	}

	// UPDATE
	public static boolean updateCommande(Commande commande) {
		String sql = "UPDATE COMMANDE SET ID_ADRESSE = ?, CODE_BON = ?, MODE_LIVRAISON = ?, STATUT = ?, ETAT_PAIEMENT = ?, MONTANT_TOTAL_CMD =? WHERE NUMERO = ?";
		boolean isUpdated = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setInt(1, commande.getAdresse().getId_adresse());
			statement.setString(2, commande.getBon_achat() == null ? null : commande.getBon_achat().getCode_bon());
			statement.setString(3, commande.getMode_livraison());
			statement.setString(4, commande.getStatut().toString());
			statement.setBoolean(5, commande.getEtat_paiement());
			statement.setFloat(6, commande.getMontant_total_cmd());
			statement.setInt(7, commande.getNumero());

			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				isUpdated = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isUpdated = false;
		}

		return isUpdated;
	}

	// DELETE
	public static boolean deleteCommande(Commande c) {
		String sql = "DELETE FROM COMMANDE WHERE NUMERO = ?";
		boolean isDeleted = false;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			for (Impression imp : c.getImpressions()) {
				deleteImpression(imp);
			}
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, c.getNumero());

			int rowsDeleted = statement.executeUpdate();
			if (rowsDeleted > 0) {
				isDeleted = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isDeleted = false;
		}
		return isDeleted;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////

	//
	// FichierPhoto
	//
	// GET
	public static FichierPhoto getFichierPhotoById(boolean photo, String chemin) {
		String sql = "SELECT * FROM FICHIERPHOTO WHERE CHEMIN = ?";
		FichierPhoto fiPhoto = null;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, chemin);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				fiPhoto = new FichierPhoto(result.getString("CHEMIN"), null, result.getString("RESOLUTION"),
						result.getDate("DATE_AJOUT"), result.getDate("DATE_NO_PHOTO"),
						result.getString("INFO_PRISE_VUE"), result.getBoolean("EST_PARTAGE"), null,
						photo ? null : getPhotosByFichierId(fiPhoto, chemin));
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return fiPhoto;
	}

	public static ArrayList<FichierPhoto> getAllFichierPhotosPartagees() {
		ArrayList<FichierPhoto> photos = new ArrayList<FichierPhoto>();
		String sql = "SELECT CHEMIN, EMAIL FROM FICHIERPHOTO WHERE EST_PARTAGE = 1";
		PreparedStatement statement;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				FichierPhoto fp = getFichierPhotoById(false, result.getString("CHEMIN"));
				Client client = getSimpleClientByEmail(result.getString("EMAIL"));
				fp.setClient(client);
				photos.add(fp);
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

		return photos;
	}

	public static ArrayList<FichierPhoto> getAllFichierPhotos() {
		ArrayList<FichierPhoto> photos = new ArrayList<FichierPhoto>();
		String sql = "SELECT CHEMIN, EMAIL FROM FICHIERPHOTO";
		PreparedStatement statement;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				FichierPhoto fp = getFichierPhotoById(false, result.getString("CHEMIN"));
				Client client = getSimpleClientByEmail(result.getString("EMAIL"));
				fp.setClient(client);
				photos.add(fp);
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

		return photos;
	}

	private static ArrayList<Photo> getPhotosByFichierId(FichierPhoto fichier, String chemin) {
		ArrayList<Photo> photos = new ArrayList<Photo>();
		String sql = "SELECT ID_PHOTO FROM PHOTO WHERE CHEMIN = ?";
		PreparedStatement statement;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, chemin);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				Photo photo = getPhotoById(true, result.getInt("ID_PHOTO"));
				photo.setFichier(fichier);
				photos.add(photo);
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}

		return photos;
	}

	private static ArrayList<Client> getClientsByFichierId(String chemin) {
		ArrayList<Client> clients = new ArrayList<Client>();
		String sql = "SELECT EMAIL FROM PARTAGE WHERE CHEMIN = ?";
		PreparedStatement statement;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, chemin);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				clients.add(getClientByEmail(result.getString("EMAIL")));
			}
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return clients;
	}

	// CREATE
	public static boolean createFichierPhoto(String chemin, String email, String resolution, String info_vue,
			boolean est_partage) {
		String sql = "INSERT INTO FICHIERPHOTO (CHEMIN, EMAIL, RESOLUTION, DATE_AJOUT, DATE_NO_PHOTO, INFO_PRISE_VUE, EST_PARTAGE) VALUES (?,?,?,?,?,?,?)";
		boolean isAdded = false;
		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, chemin);
			statement.setString(2, email);
			statement.setString(3, resolution);
			statement.setDate(4, new java.sql.Date(System.currentTimeMillis()));
			statement.setDate(5, new java.sql.Date(System.currentTimeMillis()));
			statement.setString(6, info_vue);
			statement.setBoolean(7, est_partage);

			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				isAdded = true;
			}
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isAdded = false;
		}

		return isAdded;
	}

	// UPDATE
	public static boolean updateFichierPhoto(FichierPhoto fiPhoto) {
		String sql = "UPDATE FICHIERPHOTO SET RESOLUTION = ?, INFO_PRISE_VUE = ?, EST_PARTAGE = ?, DATE_NO_PHOTO = ? WHERE CHEMIN = ?";
		boolean isUpdated = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			statement = conn.prepareStatement(sql);
			statement.setString(1, fiPhoto.getResolution());
			statement.setString(2, fiPhoto.getInfo_prise_de_vue());
			statement.setBoolean(3, fiPhoto.getEst_partage());
			statement.setDate(4, (java.sql.Date) fiPhoto.getDate_no_photo());
			statement.setString(5, fiPhoto.getChemin());
			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				isUpdated = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isUpdated = false;
		}

		return isUpdated;
	}

	// DELETE
	public static boolean deleteFichierPhoto(String chemin) {
		String sql = "DELETE FICHIERPHOTO WHERE CHEMIN = ?";
		boolean isDeleted = false;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, chemin);

			int rowsDeleted = statement.executeUpdate();
			if (rowsDeleted > 0) {
				isDeleted = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isDeleted = false;
		}
		return isDeleted;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////

	//
	// PHOTO
	//
	// GET
	public static Photo getPhotoById(boolean fichier, int id) {
		String sql = "SELECT * FROM PHOTO WHERE ID_PHOTO = ?";
		Photo photo = null;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, id);
			ResultSet result = statement.executeQuery();
			FichierPhoto fichierp = null;

			if (result.next()) {
				if (fichier == false) {
					fichierp = getFichierPhotoById(true, result.getString("CHEMIN"));
				}

				photo = new Photo(result.getInt("ID_PHOTO"), fichierp, null, result.getString("DESCRIPTION"),
						result.getString("RETOUCHE"), result.getInt("NUMERO_PAGE"), result.getInt("POSITION_X"),
						result.getInt("POSITION_Y"), result.getInt("NB_EXEMPLAIRE"));
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return photo;
	}

	public static <T extends Impression> T getImpressionById(int id) {
		T impression = null;
		String select = "SELECT TYPE_SUPPORT FROM IMPRESSION";
		String where = " WHERE ID_IMPRESSION = ?";
		String sql = "";
		PreparedStatement statement = null;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			ResultSet result = null;
			int cpt = 0;
			while (cpt < 5) {
				sql = select + where;
				statement = conn.prepareStatement(sql);
				statement.setInt(1, id);
				result = statement.executeQuery();
				if (result.next()) {
					impression = getImpressionByIdAndType(
							TypeSupport.valueOf(result.getString("TYPE_SUPPORT").toUpperCase()), id);
					cpt = 5;
				}
				cpt++;
			}

			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return impression;
	}

	// CREATE
	public static boolean createPhoto(String chemin, Impression impression, String description, int position_x,
			int position_y, int numero_page, int nb_exemplaire, String retouche) {
		String sql = "INSERT INTO PHOTO (CHEMIN, ID_IMPRESSION, DESCRIPTION, RETOUCHE, POSITION_X, POSITION_Y, NUMERO_PAGE, NB_EXEMPLAIRE) VALUES (?,?,?,?,?,?,?,?)";
		boolean isAdded = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, chemin);
			statement.setInt(2, impression.getId_impression());
			statement.setString(3, description);
			statement.setString(4, retouche);
			statement.setInt(5, position_x);
			statement.setInt(6, position_y);
			statement.setInt(7, numero_page);
			statement.setInt(8, nb_exemplaire);

			int rowsInserted = statement.executeUpdate();
			statement.close();
			if (rowsInserted > 0) {
				sql = "UPDATE FICHIERPHOTO SET DATE_NO_PHOTO = null WHERE CHEMIN = ?";
				statement = conn.prepareStatement(sql);
				statement.setString(1, chemin);
				statement.executeUpdate();
				isAdded = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isAdded = false;
		}

		return isAdded;
	}

	// UPDATE
	public static boolean updatePhoto(Photo photo) {
		String sql = "UPDATE PHOTO SET DESCRIPTION = ?, RETOUCHE = ?, POSITION_X = ?, POSITION_Y = ?, NB_EXEMPLAIRE = ? WHERE ID_PHOTO = ?";
		boolean isUpdated = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, photo.getDescription());
			statement.setString(2, photo.getRetouche());
			statement.setInt(3, photo.getPosition_X());
			statement.setInt(4, photo.getPosition_Y());
			statement.setInt(5, photo.getNb_exemplaire());
			statement.setInt(5, photo.getId_photo());
			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				isUpdated = true;
			}
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isUpdated = false;
		}

		return isUpdated;
	}

	// DELETE
	public static boolean deletePhoto(Photo p) {
		String sql = "DELETE PHOTO WHERE ID_PHOTO = ?";
		boolean isDeleted = false;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, p.getId_photo());

			int rowsDeleted = statement.executeUpdate();
			if (rowsDeleted > 0) {
				isDeleted = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isDeleted = false;
		}
		return isDeleted;
	}

	//
	// PointRelais
	//
	// GET ALL
	public static ArrayList<PointRelais> getAllPointRelais() {
		String sql = "SELECT * FROM POINT_RELAIS";
		ArrayList<PointRelais> list_point_relais = new ArrayList<PointRelais>();

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				list_point_relais.add(new PointRelais(result.getString("NOM"),
						GestionDB.getAdresseById(result.getInt("ID_ADRESSE"))));
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return list_point_relais;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////

	//
	// IMPRESSIONS
	//
	// GET

	@SuppressWarnings("unchecked")
	public static <T extends Impression> T getImpressionByIdAndType(TypeSupport type, int id) {
		String sqlImp = "SELECT * FROM IMPRESSION WHERE ID_IMPRESSION = ?";
		String sqlT = "SELECT * FROM " + type.toString() + " WHERE ID_IMPRESSION = ?";
		T t = null;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			
			// REQUETE TYPE POUR VERIFIER SI EXISTE
			PreparedStatement statementType = conn.prepareStatement(sqlT);
			statementType.setInt(1, id);
			ResultSet resultT = statementType.executeQuery();
			if (resultT.next()) {
				// REQUETE IMPRESSION POUR RECUP DONNEES
				int idT = resultT.getInt("ID_IMPRESSION");

				PreparedStatement statementImp = conn.prepareStatement(sqlImp);
				statementImp.setInt(1, idT);
				ResultSet resultImp = statementImp.executeQuery();
				if (resultImp.next()) {
					int numero = resultImp.getInt("NUMERO");
					//update montant
					if(numero == 0) {
						PreparedStatement stmt = conn.prepareStatement("SELECT CALCUL_MONTANT_IMPRESSION(?) FROM DUAL");
						stmt.setInt(1,id);       
						ResultSet rs = stmt.executeQuery();
						
						while(rs.next()) {
							  int output = rs.getInt(1);
						}
						stmt.close();
					}
					
					int nb_impression = resultImp.getInt("NB_IMPRESSION");
					int montant_total = resultImp.getInt("MONTANT_TOTAL");
					boolean etat_impression = resultImp.getBoolean("ETAT_IMPRESSION");
					Date date_impression = resultImp.getTimestamp("DATE_IMPRESSION");
					String email = resultImp.getString("EMAIL");
					Client client = new Client(email, null, null, null, null);
					Commande commande = new Commande(numero);
					Stock stock = getStockById(type, resultImp.getString("QUALITE"), resultImp.getString("FORMAT"));

					if (type == TypeSupport.AGENDA) {
						t = (T) new Agenda(idT, date_impression, nb_impression, client, stock, numero, montant_total,
								etat_impression, getAllPhotoByIdImpression(t, idT), commande,
								resultT.getString("MODELE"));
					}
					if (type == TypeSupport.ALBUM) {
						t = (T) new Album(idT, date_impression, nb_impression, client, stock, numero, montant_total,
								etat_impression, getAllPhotoByIdImpression(t, idT), commande,
								resultT.getString("TITRE"), resultT.getString("MISE_EN_PAGE"));
					}
					if (type == TypeSupport.CADRE) {
						t = (T) new Cadre(idT, date_impression, nb_impression, client, stock, numero, montant_total,
								etat_impression, getAllPhotoByIdImpression(t, idT), commande,
								resultT.getString("MISE_EN_PAGE"), resultT.getString("MODELE"));
					}
					if (type == TypeSupport.CALENDRIER) {
						t = (T) new Calendrier(idT, date_impression, nb_impression, client, stock, numero,
								montant_total, etat_impression, getAllPhotoByIdImpression(t, idT), commande,
								resultT.getString("MODELE"));
					}
					if (type == TypeSupport.TIRAGE) {
						t = (T) new Tirage(idT, date_impression, nb_impression, client, stock, numero, montant_total,
								etat_impression, getAllPhotoByIdImpression(t, idT), commande);
					}
				}
				statementType.close();
				statementImp.close();
				conn.commit();
			}
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return t;
	}

	public static <T extends Impression> ArrayList<T> getAllImpression() {
		ArrayList<T> impressions = new ArrayList<T>();
		String sql = "SELECT * FROM IMPRESSION";
		try {
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				T impression = getImpressionById(result.getInt("ID_IMPRESSION"));
				//Client client = getSimpleClientByEmail(result.getString("EMAIL"));
				if (impression != null) {
					//impression.setClient(client);
					impressions.add(impression);
				}
			}
			statement.close();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return impressions;
	}

	private static ArrayList<Photo> getAllPhotoByIdImpression(Impression imp, int idT) {
		ArrayList<Photo> photos = new ArrayList<Photo>();
		String sql = "SELECT ID_PHOTO FROM PHOTO WHERE ID_IMPRESSION = ?";

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setInt(1, idT);
			ResultSet result = statement.executeQuery();

			while (result.next()) {
				Photo photo = getPhotoById(false, result.getInt("ID_PHOTO"));
				photo.setImpression(imp);
				photos.add(photo);
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return photos;
	}

	// CREATE
	public static int createImpression(TypeSupport type, Client client, Stock stock, int numero, float montant_total,
			boolean etat_impression, int nb_impression, String modele, String titre, String mise_en_page) {
		String sqlImp = "INSERT INTO IMPRESSION(EMAIL, TYPE_SUPPORT, FORMAT, QUALITE, DATE_IMPRESSION, MONTANT_TOTAL, ETAT_IMPRESSION, NB_IMPRESSION) "
				+ "VALUES (?,?,?,?,?,?,?,?)";
		String sqlImpExt = "";
		int isAdded = -1;
		LocalDate todayLocalDate = LocalDate.now(ZoneId.systemDefault());
		java.sql.Date sqlDate = java.sql.Date.valueOf(todayLocalDate);

		PreparedStatement statementImp = null;
		PreparedStatement statementImpExt = null;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

			statementImp = conn.prepareStatement(sqlImp);
			statementImp.setString(1, client.getEmail());
			statementImp.setString(2, stock == null ? null : stock.getType_support().toString().toLowerCase());
			statementImp.setString(3, stock == null ? null : stock.getFormat());
			statementImp.setString(4, stock == null ? null : stock.getQualite());
			statementImp.setDate(5, sqlDate);
			statementImp.setFloat(6, montant_total);
			statementImp.setBoolean(7, etat_impression);
			statementImp.setInt(8, nb_impression);
			statementImp.executeUpdate();
			PreparedStatement statementID = conn
					.prepareStatement("SELECT MAX(ID_IMPRESSION) AS ID FROM IMPRESSION WHERE EMAIL = ?");
			statementID.setString(1, client.getEmail());
			ResultSet resultImp = statementID.executeQuery();
			int rowsInsertedTirage = -1;
			int id_impression = -1;

			if (resultImp.next()) {
				id_impression = resultImp.getInt("ID");
				if (type.equals(TypeSupport.AGENDA)) {
					sqlImpExt = "INSERT INTO AGENDA (ID_IMPRESSION, MODELE) " + "VALUES (?,?)";
					statementImpExt = conn.prepareStatement(sqlImpExt);
					statementImpExt.setInt(1, id_impression);
					statementImpExt.setString(2, modele);
					Agenda agenda = new Agenda(id_impression, sqlDate, nb_impression, client, stock, numero,
							montant_total, etat_impression, null, null, modele);
					client.addImpression(agenda);

				} else if (type.equals(TypeSupport.ALBUM)) {
					sqlImpExt = "INSERT INTO ALBUM (ID_IMPRESSION, TITRE, MISE_EN_PAGE) " + "VALUES (?,?,?)";
					statementImpExt = conn.prepareStatement(sqlImpExt);
					statementImpExt.setInt(1, id_impression);
					statementImpExt.setString(2, titre);
					statementImpExt.setString(3, mise_en_page);
					Album album = new Album(id_impression, sqlDate, nb_impression, client, stock, numero, montant_total,
							etat_impression, null, null, modele, mise_en_page);
					client.addImpression(album);

				} else if (type.equals(TypeSupport.CADRE)) {
					sqlImpExt = "INSERT INTO CADRE (ID_IMPRESSION, MISE_EN_PAGE, MODELE)" + " VALUES (?,?,?)";
					statementImpExt = conn.prepareStatement(sqlImpExt);
					statementImpExt.setInt(1, id_impression);
					statementImpExt.setString(2, mise_en_page);
					statementImpExt.setString(3, modele);
					Cadre cadre = new Cadre(id_impression, sqlDate, nb_impression, client, stock, numero, montant_total,
							etat_impression, null, null, mise_en_page, modele);
					client.addImpression(cadre);

				} else if (type.equals(TypeSupport.CALENDRIER)) {
					sqlImpExt = "INSERT INTO CALENDRIER (ID_IMPRESSION, MODELE) " + "VALUES (?,?)";
					statementImpExt = conn.prepareStatement(sqlImpExt);
					statementImpExt.setInt(1, id_impression);
					statementImpExt.setString(2, modele);
					Calendrier calendrier = new Calendrier(id_impression, sqlDate, nb_impression, client, stock, numero,
							montant_total, etat_impression, null, null, modele);
					client.addImpression(calendrier);

				} else if (type.equals(TypeSupport.TIRAGE)) {
					sqlImpExt = "INSERT INTO TIRAGE (ID_IMPRESSION)" + " VALUES (?)";
					statementImpExt = conn.prepareStatement(sqlImpExt);
					statementImpExt.setInt(1, id_impression);
					Tirage tirage = new Tirage(id_impression, sqlDate, nb_impression, client, stock, numero,
							montant_total, etat_impression, null, null);
					client.addImpression(tirage);
				}

				rowsInsertedTirage = statementImpExt.executeUpdate();
			}
			statementImpExt.close();
			statementImp.close();
			conn.commit();

			if (rowsInsertedTirage > 0) {
				isAdded = id_impression;
			}
		} catch (SQLException e) { popupException(e);
			isAdded = -1;
		}

		return isAdded;
	}

	// UPDATE
	public static <T extends Impression> boolean updateImpression(TypeSupport type, T impression) {
		String sqlImp;
		String sqlImpExt = "";
		boolean isUpdated = false;

		PreparedStatement statement = null;
		PreparedStatement statementImp = null;
		try {
			if(impression.getCommande() == null || impression.getCommande().getNumero() == 0) {
				sqlImp = "UPDATE IMPRESSION SET TYPE_SUPPORT = ?, FORMAT = ?, QUALITE = ?, NUMERO = NULL, MONTANT_TOTAL = ?, ETAT_IMPRESSION = ?, NB_IMPRESSION = ? WHERE ID_IMPRESSION = ?";
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				statement = conn.prepareStatement(sqlImp);
				statement.setString(1, impression.getStock().getType_support().toString().toLowerCase());
				statement.setString(2, impression.getStock().getFormat());
				statement.setString(3, impression.getStock().getQualite());
				statement.setFloat(4, impression.getMontant_total());
				statement.setBoolean(5, impression.getEtat_impression());
				statement.setInt(6, impression.getNb_impression());
				statement.setInt(7, impression.getId_impression());
			}else {
				sqlImp = "UPDATE IMPRESSION SET TYPE_SUPPORT = ?, FORMAT = ?, QUALITE = ?, NUMERO = ?, MONTANT_TOTAL = ?, ETAT_IMPRESSION = ?, NB_IMPRESSION = ? WHERE ID_IMPRESSION = ?";
				conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
				statement = conn.prepareStatement(sqlImp);
				statement.setString(1, impression.getStock().getType_support().toString().toLowerCase());
				statement.setString(2, impression.getStock().getFormat());
				statement.setString(3, impression.getStock().getQualite());
				statement.setInt(4, impression.getCommande().getNumero());
				statement.setFloat(5, impression.getMontant_total());
				statement.setBoolean(6, impression.getEtat_impression());
				statement.setInt(7, impression.getNb_impression());
				statement.setInt(8, impression.getId_impression());
			}
			

			int rowsInsertedImpEx = 0;
			int rowsInsertedImp = -1;

			if (type.equals(TypeSupport.AGENDA)) {
				Agenda agenda = (Agenda) impression;
				sqlImpExt = "UPDATE AGENDA SET MODELE = ? WHERE ID_IMPRESSION = ?";
				statementImp = conn.prepareStatement(sqlImpExt);
				statementImp.setString(1, agenda.getModele());
				statementImp.setInt(2, agenda.getId_impression());
				rowsInsertedImpEx = statementImp.executeUpdate();
				statementImp.close();


			}
			if (type.equals(TypeSupport.ALBUM)) {
				Album album = (Album) impression;
				sqlImpExt = "UPDATE ALBUM SET TITRE = ?, MISE_EN_PAGE = ? WHERE ID_IMPRESSION = ?";
				statementImp = conn.prepareStatement(sqlImpExt);
				statementImp.setString(1, album.getTitre());
				statementImp.setString(2, album.getMise_en_page());
				statementImp.setInt(3, album.getId_impression());
				rowsInsertedImpEx = statementImp.executeUpdate();
				statementImp.close();


			}
			if (type.equals(TypeSupport.CADRE)) {
				Cadre cadre = (Cadre) impression;
				sqlImpExt = "UPDATE CADRE SET MISE_EN_PAGE = ?, MODELE = ? WHERE ID_IMPRESSION = ?";
				statementImp = conn.prepareStatement(sqlImpExt);
				statementImp.setString(1, cadre.getMise_en_page());
				statementImp.setString(2, cadre.getModele());
				statementImp.setInt(3, cadre.getId_impression());
				rowsInsertedImpEx = statementImp.executeUpdate();
				statementImp.close();


			}
			if (type.equals(TypeSupport.CALENDRIER)) {
				Calendrier calendrier = (Calendrier) impression;
				sqlImpExt = "UPDATE CALENDRIER SET MODELE = ? WHERE ID_IMPRESSION = ?";
				statementImp = conn.prepareStatement(sqlImpExt);
				statementImp.setString(1, calendrier.getModele());
				statementImp.setInt(2, calendrier.getId_impression());
				rowsInsertedImpEx = statementImp.executeUpdate();
				statementImp.close();


			}
			if (type.equals(TypeSupport.TIRAGE)) {
				rowsInsertedImpEx = 1;
			}
			rowsInsertedImp = statement.executeUpdate();
			if (rowsInsertedImp > 0 && rowsInsertedImpEx > 0) {
				isUpdated = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isUpdated = false;
		}

		return isUpdated;
	}

	public static boolean updateImp() {
		String sql = "UPDATE IMPRESSION SET DATE_IMPRESSION = ? WHERE ID_IMPRESSION = ?";
		boolean isUpdated = false;
		LocalDate todayLocalDate = LocalDate.now(ZoneId.systemDefault());
		java.sql.Date sqlDate = java.sql.Date.valueOf(todayLocalDate);
		PreparedStatement statement;

		try {
			statement = conn.prepareStatement(sql);
			statement.setDate(1, sqlDate);
			statement.setInt(2, 37);
			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				isUpdated = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isUpdated = false;
		}

		return isUpdated;
	}

	// DELETE
	public static boolean deleteImpression(Impression impression) {
		String sqlImp = "DELETE IMPRESSION WHERE ID_IMPRESSION = ?";
		String sqlImpExt = "DELETE ? WHERE ID_IMPRESSION = ?";
		boolean isDeleted = false;

		try {
			for (Photo p : impression.getPhotos()) {
				deletePhoto(p);
			}
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statementImp = conn.prepareStatement(sqlImp);
			PreparedStatement statementImpExt = conn.prepareStatement(sqlImpExt);

			statementImp.setString(1, impression.getClass().getSimpleName().toUpperCase());
			statementImp.setInt(2, impression.getId_impression());

			statementImpExt.setInt(1, impression.getId_impression());

			int rowsDeletedImp = statementImp.executeUpdate();
			int rowsDeletedImpExt = statementImpExt.executeUpdate();
			if (rowsDeletedImp > 0 && rowsDeletedImpExt > 0) {
				isDeleted = true;
			}
			statementImp.close();
			statementImpExt.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isDeleted = false;
		}
		return isDeleted;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////

	//
	// BON_ACHAT
	//
	// GET
	public static BonAchat getBonAchatById(String id) {
		String sql = "SELECT * FROM BON_ACHAT WHERE CODE_BON = ?";
		BonAchat bon_achat = null;
		Commande commande = null;
		Commande commandeG = null;
		Client client = null;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, id);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				/*
				 * if (result.getInt("NUMERO") >= 0) { commande =
				 * getCommandeById(result.getInt("NUMERO")); } if
				 * (result.getInt("NUMERO_GENERE") >= 0) { commandeG =
				 * getCommandeById(result.getInt("NUMERO_GENERE")); } if
				 * (result.getString("EMAIL") != null) { client =
				 * getClientByEmail(result.getString("EMAIL")); }
				 */
				bon_achat = new BonAchat(result.getString("CODE_BON"), commande, commandeG, client,
						result.getInt("POURCENTAGEREDUC"), result.getString("TYPE_BONACHAT"));
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return bon_achat;
	}

	public static ArrayList<BonAchat> getBonAchatByEmail(String email) {
		String sql = "SELECT * FROM BON_ACHAT WHERE EMAIL = ? AND NUMERO IS NULL";
		ArrayList<BonAchat> lesBonsAchats = new ArrayList<BonAchat>();
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, email);
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				lesBonsAchats.add(new BonAchat(result.getString("CODE_BON"), null, null, null,
						result.getInt("POURCENTAGEREDUC"), result.getString("TYPE_BONACHAT")));
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return lesBonsAchats;
	}

	// CREATE
	public static boolean createBonAchat(int numeroC, int numeroCGenere, String email, int pourcentage,
			String type_bon_achat) {
		String sql = "INSERT INTO BON_ACHAT (NUMERO, NUMERO_GENERE, EMAIL, POURCENTAGEREDUC, TYPE_BONACHAT) VALUES (?,?,?,?,?)";
		boolean isAdded = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setInt(1, numeroC);
			statement.setInt(2, numeroCGenere);
			statement.setString(3, email);
			statement.setInt(4, pourcentage);
			statement.setString(5, type_bon_achat);

			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				isAdded = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isAdded = false;
		}

		return isAdded;
	}

	// UPDATE
	public static boolean upadateBonAchat(BonAchat bon) {
		String sql = "UPDATE BON_ACHAT SET NUMERO = ?, NUMERO_GENERE = ?, EMAIL = ?, POURCENTAGEREDUC = ?, TYPE_BONACHAT = ? WHERE CODE_BON = ?";
		boolean isUpdated = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setInt(1, bon.getCommande().getNumero());
			statement.setInt(2, bon.getCommandeGeneree().getNumero());
			statement.setString(3, bon.getClient().getEmail());
			statement.setInt(4, bon.getPourcentage_reduc());
			statement.setString(5, bon.getType_bon_achat());
			statement.setString(5, bon.getCode_bon());
			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				isUpdated = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isUpdated = false;
		}

		return isUpdated;
	}

	// DELETE
	public static boolean deleteBonAchat(String code_bon) {
		String sql = "DELETE BON_ACHAT WHERE CODE_BON = ?";
		boolean isDeleted = false;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, code_bon);

			int rowsDeleted = statement.executeUpdate();
			if (rowsDeleted > 0) {
				isDeleted = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isDeleted = false;
		}
		return isDeleted;
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////

	//
	// STOCK
	//
	// GET
	public static ArrayList<Stock> getAllStock() {
		String sql = "SELECT * FROM STOCK";
		ArrayList<Stock> stocks = null;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			ResultSet result = statement.executeQuery();
			stocks = new ArrayList<Stock>();
			while (result.next()) {
				Stock stock = new Stock(TypeSupport.valueOf(result.getString("TYPE_SUPPORT").toUpperCase()),
						result.getString("QUALITE"), result.getString("FORMAT"), result.getInt("QUANTITE"),
						result.getInt("PRIX"));
				stocks.add(stock);
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return stocks;
	}

	public static Stock getStockById(TypeSupport type, String qualite, String format) {
		String sql = "SELECT * FROM STOCK WHERE TYPE_SUPPORT = ? AND QUALITE = ? AND FORMAT = ?";
		Stock stock = null;
		String type_s = type.toString().toLowerCase();
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, type_s);
			statement.setString(2, qualite);
			statement.setString(3, format);
			ResultSet result = statement.executeQuery();
			if (result.next()) {
				stock = new Stock(TypeSupport.valueOf(result.getString("TYPE_SUPPORT").toUpperCase()),
						result.getString("QUALITE"), result.getString("FORMAT"), result.getInt("QUANTITE"),
						result.getInt("PRIX"));

			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			e.printStackTrace();
		}
		return stock;
	}

	// CREATE
	public static boolean createStock(TypeSupport type, String qualite, String format, int quantite, int prix) {
		String sql = "INSERT INTO STOCK (TYPE_SUPPORT, QUALITE, FORMAT, QUANTITE, PRIX) VALUES (?,?,?,?,?)";
		boolean isAdded = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
			statement = conn.prepareStatement(sql);
			statement.setString(1, type.toString().toLowerCase());
			statement.setString(2, qualite);
			statement.setString(3, format);
			statement.setInt(4, quantite);
			statement.setInt(5, prix);

			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				isAdded = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isAdded = false;
		}

		return isAdded;
	}

	// UPDATE
	public static boolean updateStock(Stock stock) {
		String sql = "UPDATE STOCK SET QUANTITE = ?, PRIX = ? WHERE TYPE_SUPPORT = ? AND QUALITE = ? AND FORMAT = ?";
		boolean isUpdated = false;

		PreparedStatement statement;
		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			statement = conn.prepareStatement(sql);
			statement.setInt(1, stock.getQuantite());
			statement.setInt(2, stock.getPrix());
			statement.setString(3, stock.getType_support().toString().toLowerCase());
			statement.setString(4, stock.getQualite());
			statement.setString(5, stock.getFormat());

			int rowsInserted = statement.executeUpdate();
			if (rowsInserted > 0) {
				isUpdated = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isUpdated = false;
		}

		return isUpdated;
	}

	// DELETE
	public static boolean deleteStock(TypeSupport type, String qualite, String format) {
		String sql = "DELETE STOCK WHERE TYPE_SUPPORT = ? AND QUALITE = ? AND FORMAT = ?";
		boolean isDeleted = false;

		try {
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
			PreparedStatement statement = conn.prepareStatement(sql);
			statement.setString(1, type.toString().toLowerCase());
			statement.setString(2, qualite);
			statement.setString(3, format);

			int rowsDeleted = statement.executeUpdate();
			if (rowsDeleted > 0) {
				isDeleted = true;
			}
			statement.close();
			conn.commit();
		} catch (SQLException e) { popupException(e);
			isDeleted = false;
		}
		return isDeleted;
	}
	
	public static void popupException(Exception ex) {
		Stage popupwindow=new Stage();	      
		popupwindow.initModality(Modality.APPLICATION_MODAL);
		popupwindow.setTitle("Trigger exception");
		Label texte = new Label(ex.getMessage().split("\n", 2)[0]);
		Button button= new Button("Ok");
		button.setOnAction(e -> popupwindow.close());
		VBox layout= new VBox(10);
		layout.getChildren().addAll(texte, button);
		layout.setAlignment(Pos.CENTER);
		Scene scene= new Scene(layout, 600, 250);
		popupwindow.setScene(scene);
		popupwindow.showAndWait();
	}

}
