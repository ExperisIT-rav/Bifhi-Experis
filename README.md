# Bifhi-Experis


Interface utilisateur custom pour HPE Idol.
Vous devez avoir installé sur votre poste NodeJS, Java8 et maven pour lancer ce projet ainsi que la solution HPE Idol.

Pour compiler le projet lancer la commande suivante suivante dans le répertoire du projet : 
	mvn clean install -DskipTests=true

Pour lancer l'interface via SpringBoot : 
	- se déplacer dans le répertoire find-idol
	- mvn spring-boot:run -Dhp.find.home=[CheminVersLeProjet]\find-idol\home
	
Une fois que des modifications sont faites, il faut compiler le projet de nouveau mais l'application lancé par SpringBoot sera mise à jour par la recompilation.