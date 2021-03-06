package interfaces.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import DB.GestionDB;
import DB.LocalDataClient;
import DTO.Commande;
import DTO.Impression;
import DTO.StatutCommande;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;


public class ChoisirCommandeController implements Initializable {
	
	@FXML
	private ComboBox<Commande> lesCommandesEnCours;
	
	private int idImpression;
	
	private Impression impression;

	public void setObjects(int idImpress) {
		this.idImpression = idImpress;
		refresh();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	public void refresh() {
		LocalDataClient.refresh();
		ArrayList<Commande> lesCmdEnCours = new ArrayList<Commande>();
		
		for(Commande cmd : LocalDataClient.client.getCommandes()) {
			if(cmd.getStatut().equals(StatutCommande.En_cours)) {
				System.out.println(cmd.toString());
				lesCmdEnCours.add(cmd);
			}
		}
		lesCommandesEnCours.getItems().setAll(lesCmdEnCours);
	}
	
	@FXML
	 void addCommande(MouseEvent event) {
		 if((-1)!=GestionDB.createCommande(0, LocalDataClient.client.getEmail(), " ", StatutCommande.En_cours, false, (float) 0.00)) {
			 this.popup("Création de commande", "Une nouvelle commande a été crée", "Fermer");
		 }
		 else {
			 this.popup("Création de commande", "La création de commande a échouée !", "Fermer");
		 }
		 refresh();
	 }
	
	@FXML
	 void retour(MouseEvent event) {
		  	int i = idImpression;
		  	
		 	FXMLLoader Loader = new FXMLLoader();
		    Loader.setLocation(getClass().getResource("/interfaces/views/GestionImpression.fxml"));
		    try {
				Loader.load();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		    GestionImpressionController controller = Loader.getController();
		    controller.setObjects(i);

		    Parent home_page_parent = Loader.getRoot();
		    Scene home_page_scene = new Scene(home_page_parent);
		    Stage app_stage = (Stage) ((Node) event.getSource()).getScene()
			    .getWindow();
		    app_stage.setScene(home_page_scene);
		    app_stage.show();	
	 }
	
	@FXML
	 void valider(MouseEvent event) {
		impression = GestionDB.getImpressionById(idImpression);
		impression.setCommande(lesCommandesEnCours.getSelectionModel().getSelectedItem());
		int idCommande = lesCommandesEnCours.getSelectionModel().getSelectedItem().getNumero();
	
		if(GestionDB.updateImpression(impression.getStock().getType_support(), impression)) {
			this.popup("Commande", "L'impression a été ajoutée à la commande !", "Fermer");
		}
		else {
			this.popup("Commande", "L'ajout n'a pas fonctionné !", "Fermer");
		}
			
		 
		int i = idCommande;
		  	
	 	FXMLLoader Loader = new FXMLLoader();
	    Loader.setLocation(getClass().getResource("/interfaces/views/PasserCommande.fxml"));
	    try {
			Loader.load();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    PasserCommandeController controller = Loader.getController();
	    controller.setObjects(i);

	    Parent home_page_parent = Loader.getRoot();
	    Scene home_page_scene = new Scene(home_page_parent);
	    Stage app_stage = (Stage) ((Node) event.getSource()).getScene()
		    .getWindow();
	    app_stage.setScene(home_page_scene);
	    app_stage.show();	
	 }
	
	void popup(String title, String label, String buttonText) {
		Stage popupwindow=new Stage();	      
		popupwindow.initModality(Modality.APPLICATION_MODAL);
		popupwindow.setTitle(title);
		Label texte = new Label(label);
		Button button= new Button(buttonText);
		button.setOnAction(e -> popupwindow.close());
		VBox layout= new VBox(10);
		layout.getChildren().addAll(texte, button);
		layout.setAlignment(Pos.CENTER);
		Scene scene= new Scene(layout, 300, 250);
		popupwindow.setScene(scene);
		popupwindow.showAndWait();
	}
}
