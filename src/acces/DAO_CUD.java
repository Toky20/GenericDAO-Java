package acces;

import annotation.Colonne;
import annotation.PrimaryKey;
import annotation.Table;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class DAO_CUD extends DAORead {
    
    /**
    * Permet d'éxécuter une requete vers une base
    *
    * @param con La connexion vers la base
    * @param query La requete 
    * @example
    * ```java
    * // Import nécessaire
    * import java.sql.Connection;
    * 
    // Exemple d'utilisation pour créer un nouvel utilisateur dans une base de données
    Connection connection = // Votre connexion à la base de données
    String query = "INSERT INTO utilisateurs (nom, email, mot_de_passe) VALUES ('Durant', 'email@flop.lol', 'password')";

    execute(connection, query);
    * ```
    */
    public <T> void execute(Connection con, String query) throws Exception {
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(query);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }
    
    /**
    * Insère une ligne vers une SGBD à partir d'un objet
    *
    * @param con La connexion vers la base 
    * @param obj L'objet contenant les données
    * @example
    * ```java
    * // Import nécessaire
    * import java.sql.Connection;
    * import java.util.Date;

    // Hypothetical object class with @Table, @Colonne, and @PrimaryKey annotations (replace with yours)
    @Table(nom = "produits")
    public class Produit {
        @PrimaryKey
        private int id; // Auto-incrémentée en base de données
        @Colonne(nom = "nom_produit")
        private String nom;
        private double prix;
        private Date dateAjout; // Supposez que la date est gérée automatiquement
        // Getters et Setters (non affichés pour la concision)
    }

    // Exemple d'utilisation pour insérer un nouveau produit
    Connection connection = // Votre connexion à la base de données
    Produit produit = new Produit();
    produit.setNom("Clavier");
    produit.setPrix(49.99);

    insert(connection, produit);
    */
    public <T> void insert(Connection con, T obj) throws Exception {
        // Retrieve table and column information
        Class<?> clazz = obj.getClass();
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        String tableName = tableAnnotation != null ? tableAnnotation.nom() : clazz.getSimpleName().toLowerCase();

        List<String> columnNamesList = new ArrayList<>();
        List<Object> valuesList = new ArrayList<>();

        // Separate primary key handling
        Field primaryKeyField = null;

        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);

                // Check for both @PrimaryKey and @Colonne annotations
                if (field.getAnnotation(PrimaryKey.class) != null) {
                    primaryKeyField = field;
                    continue; // Skip primary key for insertion
                } else if (field.getAnnotation(Colonne.class) != null) {
                    Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                    String columnName = colonneAnnotation != null ? colonneAnnotation.nom() : field.getName();
                    columnNamesList.add(columnName);
                }

                Method getterMethod = clazz.getMethod(getterName);
                Object value = getterMethod.invoke(obj);

                // Type-specific handling for non-primary key fields
                if (value instanceof String) {
                    valuesList.add("'" + value + "'");
                } else if (value instanceof Number) {
                    valuesList.add(value.toString());
                } else if (value instanceof Date) {
                    valuesList.add("'" + ((Date) value).toString() + "'");
                } else {
                    valuesList.add(value);
                }
            }
        }

        // Handle potential missing primary key information
        if (primaryKeyField == null) {
            throw new IllegalArgumentException("Primary key field not found for object: " + obj);
        }

        // Construct query excluding primary key
        String columnNames = String.join(", ", columnNamesList);
        String values = valuesList.stream()
                .map(Object::toString)
                .collect(Collectors.joining(", "));
        String query = "INSERT INTO " + tableName + " (" + columnNames + ") VALUES (" + values + ");";

        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(query);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
    * Mettre à jour une ou des ligne(s) vers une SGBD à partir d'un objet
    *
    * @param con La connexion vers la base 
    * @example
    * ```java
    * // Import nécessaire
    * import java.sql.Connection;
    * import java.util.Date;

    // Hypothetical object class with @Table, @Colonne, and @PrimaryKey annotations (replace with yours)
    @Table(nom = "commandes")
    public class Commande {
        @PrimaryKey
        private int idCommande;
        @Colonne(nom = "date_commande")
        private Date dateCommande;
        private double montant;
        // Getters et Setters (non affichés pour la concision)
    }

    // Exemple d'utilisation pour mettre à jour une commande existante
    Connection connection = // Votre connexion à la base de données
    Commande commande = new Commande();
    commande.setIdCommande(10); // ID de la commande à mettre à jour
    commande.setDateCommande(Date.valueOf("2024-07-12")); // Nouvelle date de commande
    commande.setMontant(69.99); // Nouveau montant

    update(connection, commande);
    * @param obj L'objet contenant les données
    */
    public <T> void update(Connection con, T obj) throws Exception {
        // Retrieve table and column information
        Class<?> clazz = obj.getClass();
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        String tableName = tableAnnotation != null ? tableAnnotation.nom() : clazz.getSimpleName().toLowerCase();

        // Separate primary key handling
        String primaryKeyField = null;
        Object primaryKeyValue = null;

        // Identify primary key field and value
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())) {
                if (field.getAnnotation(PrimaryKey.class) != null) {
                    field.setAccessible(true);
                    primaryKeyField = field.getName();
                    Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                    if (colonneAnnotation != null) {
                        primaryKeyField = colonneAnnotation.nom();
                    }
                    primaryKeyValue = field.get(obj);
                    break;
                }
            }
        }

        // Handle missing primary key
        if (primaryKeyField == null || primaryKeyValue == null) {
            throw new IllegalArgumentException("Primary key field or value not found for object: " + obj);
        }

        // Construct SET clause
        List<String> updatePairs = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers()) && !field.equals(primaryKeyField)) {
                String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                Method getterMethod = clazz.getMethod(getterName);
                Object value = getterMethod.invoke(obj);

                String columnName = field.getAnnotation(Colonne.class) != null ? field.getAnnotation(Colonne.class).nom() : field.getName();
                String updatePair = columnName + " = " + getValueString(value);
                updatePairs.add(updatePair);
            }
        }

        String setClause = String.join(", ", updatePairs);

        // Construct UPDATE query
        String query = "UPDATE " + tableName + " SET " + setClause + " WHERE " + primaryKeyField + " = " + getValueString(primaryKeyValue);

        // Print query for debugging (optional)
        System.out.println(query);

        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(query);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    /**
    * Supprimer une ou des ligne(s) vers une SGBD à partir d'un objet
    *
    * @param con La connexion vers la base
    * @example
    * ```java
    * // Import nécessaire
    * import java.sql.Connection;

    // Hypothetical object class with @Table and @PrimaryKey annotations (replace with yours)
    @Table(nom = "clients")
    public class Client {
        @PrimaryKey
        private int idClient;
        private String nom;
        private String email;
        // Getters et Setters (non affichés pour la concision)
    }

    // Exemple d'utilisation pour supprimer un client existant
    Connection connection = // Votre connexion à la base de données
    Client client = new Client();
    client.setIdClient(25); // ID du client à supprimer

    delete(connection, client);
    * @param obj L'objet contenant les données
    */
    public <T> void delete(Connection con, T obj) throws Exception {
        // Retrieve table and primary key information similar to the `update` function
        Class<?> clazz = obj.getClass();
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        String tableName = tableAnnotation != null ? tableAnnotation.nom() : clazz.getSimpleName().toLowerCase();

        String primaryKeyField = null;
        Object primaryKeyValue = null;
        for (Field field : clazz.getDeclaredFields()) {
            if (field.getAnnotation(PrimaryKey.class) != null) {
                primaryKeyField = field.getName();
                Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                if (colonneAnnotation != null) {
                    primaryKeyField = colonneAnnotation.nom();
                }
                primaryKeyValue = clazz.getMethod("get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1)).invoke(obj);
                break;
            }
        }

        if (primaryKeyField == null || primaryKeyValue == null) {
            throw new IllegalArgumentException("Primary key field and value not found for object: " + obj);
        }

        String query = "DELETE FROM " + tableName + " WHERE " + primaryKeyField + " = " + (primaryKeyValue instanceof String ? "'" + primaryKeyValue + "'" : primaryKeyValue);

        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(query);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }
    
    private String getValueString(Object value) {
        if (value instanceof String) {
            return "'" + value + "'";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Date) {
            return "'" + new SimpleDateFormat("yyyy-MM-dd").format((Date) value) + "'";
        } else {
            return value.toString();
        }
    }
 
}
