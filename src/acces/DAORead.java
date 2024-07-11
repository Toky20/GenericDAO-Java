/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package acces;

import annotation.Colonne;
import annotation.Table;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class DAORead {
    
    /**
    * On entre une connexion et une requete et on obtient les données
    * sous-forme de liste d'objets
    * 
     * @param <T> Le type
     * @param co La connexion au SGBD
     * @param o L'objet à mapper
     * @param query La requete
     * @example
     * ```java
    * // Import nécessaire
    * import java.sql.Connection;
    * import java.util.ArrayList;
    * import java.util.List;

    // Hypothetical object class (remplacez par votre classe réelle)
    public class Personne {
        private String nom;
        private int age;
        // Getters et Setters (non affichés pour la concision)
    }

    // Exemple d'utilisation
    Connection connection ; // Votre connexion à la base de données
    Personne personne = new Personne();
    String requete = "SELECT * FROM personnes WHERE nom = Jean";

    List<Personne> personnes = find(connection, personne, requete);
    for (Personne p : personnes) {
        System.out.println(p.getNom() + " - " + p.getAge());
    }
     * 
     * ```
    * @return Liste d'objets représentant la requete envoyé
    */
    public <T> List<T> find(Connection co, T o, String query) throws Exception {
        Class<?> clazz = o.getClass();
        List<T> results;
        try (Statement stmt = co.createStatement(); 
            ResultSet rs = stmt.executeQuery(query)) {
            results = new ArrayList<>();
            while (rs.next()) {
                T instance = (T) clazz.newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        String columnName = field.getName();
                        Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                        if (colonneAnnotation != null) {
                            columnName = colonneAnnotation.nom();
                        }
                        String setterName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                        Method setterMethod = clazz.getMethod(setterName, field.getType());
                        Object value = rs.getObject(columnName);
                        // Conversion de type
                        if (value instanceof String) {
                            value = value.toString().replace("'", "''");
                        }
                        setterMethod.invoke(instance, value);
                    }
                }
                results.add(instance);
            }          
        }
        return results;
    }

    /**
    * On entre une connexion et un objet et on obtient les données
    * sous-forme de liste d'objets
    * 
     * @param co La connexion au SGBD
     * @param o L'objet à mapper
     * @example
     * ```java
     * // Import nécessaire
    * import java.sql.Connection;
    * import java.util.ArrayList;
    * import java.util.List;

    // Hypothetical object class with @Table annotation (replace with yours)
    @Table(nom = "clients")
    public class Client {
        private int id;
        private String nom;
        private String email;
        // Getters et Setters (non affichés pour la concision)
    }

    // Exemple d'utilisation
    Connection connection = // Votre connexion à la base de données
    Client client = new Client();

    List<Client> clients = findAll(connection, client);
    for (Client c : clients) {
        System.out.println(c.getId() + " - " + c.getNom() + " - " + c.getEmail());
    }
     * ```
    * @return Liste d'objets d'un "SELECT *" d'une SGBD
    */
    public <T> List<T> findAll(Connection co, T o) throws Exception {
        Class<?> clazz = o.getClass();
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        String tableName = tableAnnotation != null ? tableAnnotation.nom() : clazz.getSimpleName().toLowerCase();
        String query = "SELECT * FROM " + tableName + ";";
        List<T> results;
        try (Statement stmt = co.createStatement(); 
            ResultSet rs = stmt.executeQuery(query)) {
            results = new ArrayList<>();
            while (rs.next()) {
                T instance = (T) clazz.newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        String columnName = field.getName();
                        Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                        if (colonneAnnotation != null) {
                            columnName = colonneAnnotation.nom();
                        }
                        String setterName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                        Method setterMethod = clazz.getMethod(setterName, field.getType());
                        Object value = rs.getObject(columnName);
                        // Conversion de type
                        if (value instanceof String) {
                            value = value.toString().replace("'", "''");
                        }
                        setterMethod.invoke(instance, value);
                    }
                }
                results.add(instance);
            }          
        }
        return results;
    }

    /**
    * On entre une connexion et un objet et on obtient les données
    * sous-forme de liste d'objets avec de la pagination
    * 
     * @param co La connexion au SGBD
     * @param o L'objet à mapper
     * @param pageNumber Le numéro de page
     * @param pageSize Le nombre d'éléments par page
     * * @example
     * ```java
     * // Import nécessaire
    * import java.sql.Connection;
    * import java.util.ArrayList;
    * import java.util.List;

    // Hypothetical object class with @Table annotation (replace with yours)
    @Table(nom = "produits")
    public class Produit {
        private int id;
        private String nom;
        private double prix;
        // Getters et Setters (non affichés pour la concision)
    }

    // Exemple d'utilisation pour la page 2 avec 10 éléments par page
    Connection connection = // Votre connexion à la base de données
    Produit produit = new Produit();
    int pageNumber = 2;
    int pageSize = 10;

    List<Produit> produits = findAll(connection, produit, pageNumber, pageSize);
    for (Produit p : produits) {
        System.out.println(p.getId() + " - " + p.getNom() + " - " + p.getPrix());
    }
     * ```
    * @return Liste d'objets d'un "SELECT *" d'une SGBD
    */
    public <T> List<T> findAll(Connection co, T o, int pageNumber, int pageSize) throws Exception {
        Class<?> clazz = o.getClass();
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        String tableName = tableAnnotation != null ? tableAnnotation.nom() : clazz.getSimpleName().toLowerCase();
        // Calculate offset based on page number and page size
        int offset = (pageNumber - 1) * pageSize;
        // Construct paginated SQL query
        String query = "SELECT * FROM " + tableName + " LIMIT " + pageSize + " OFFSET " + offset + ";";
        List<T> results;
        try (Statement stmt = co.createStatement(); 
             ResultSet rs = stmt.executeQuery(query)) {
            results = new ArrayList<>();
            while (rs.next()) {
                T instance = (T) clazz.newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        String columnName = field.getName();
                        Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                        if (colonneAnnotation != null) {
                            columnName = colonneAnnotation.nom();
                        }
                        String setterName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                        Method setterMethod = clazz.getMethod(setterName, field.getType());
                        Object value = rs.getObject(columnName);
                        if (value instanceof String) {
                            value = value.toString().replace("'", "''");
                        }
                        setterMethod.invoke(instance, value);
                    }
                }
                results.add(instance);
            }          
        }
        return results;
    }

    /**
    * On entre une connexion et un objet qui définira les critères WHERE et on obtient les données
    * sous-forme de liste d'objets 
    * 
     * @param co La connexion au SGBD
     * @param o L'objet à mapper
     * * @example
     * ```java
     * // Import nécessaire
    * import java.sql.Connection;
    * import java.util.ArrayList;
    * import java.util.List;

    // Hypothetical object class with @Table annotation (replace with yours)
    @Table(nom = "clients")
    public class Client {
        private int id;
        private String nom;
        private String email;
        // Getters et Setters (non affichés pour la concision)
    }

    // Exemple d'utilisation
    Connection connection = // Votre connexion à la base de données
    Client client = new Client();
    * client.setNom("Jean");

    List<Client> clients = findByCriteria(connection, client);
    for (Client c : clients) {
        System.out.println(c.getId() + " - " + c.getNom() + " - " + c.getEmail());
    }
     * ```
    * @return Liste d'objets d'un "SELECT *" d'une SGBD avec un WHERE
    */
    public <T> List<T> findByCriteria(Connection co, T o) throws Exception {
        Class<?> clazz = o.getClass();
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        String tableName = tableAnnotation != null ? tableAnnotation.nom() : clazz.getSimpleName().toLowerCase();
        // Build WHERE clause dynamically based on non-null fields
        StringBuilder whereClause = new StringBuilder();
        List<Object> valuesList = new ArrayList<>();
        int counter = 0;
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                Method getterMethod = clazz.getMethod(getterName);
                Object value = getterMethod.invoke(o);
                if (value != null) {
                    if (counter > 0) {
                        whereClause.append(" OR ");
                    } else {
                        whereClause.append(" WHERE ");
                    }
                    String columnName = field.getName();
                    Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                    if (colonneAnnotation != null) {
                        columnName = colonneAnnotation.nom();
                    }
                    whereClause.append(columnName + " = ?");
                    valuesList.add(value);
                    counter++;
                }
            }
        }
        String query = "SELECT * FROM " + tableName + " " + whereClause.toString() + ";";
        List<T> results;
        // Set values for prepared statement
        try (PreparedStatement stmt = co.prepareStatement(query)) {
            // Set values for prepared statement
            for (int i = 0; i < valuesList.size(); i++) {
                stmt.setObject(i + 1, valuesList.get(i));
            }  try (ResultSet rs = stmt.executeQuery()) {
                results = new ArrayList<>();
                while (rs.next()) {
                    T instance = (T) clazz.newInstance();
                    for (Field field : clazz.getDeclaredFields()) {
                        if (!Modifier.isStatic(field.getModifiers())) {
                            String columnName = field.getName();
                            Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                            if (colonneAnnotation != null) {
                                columnName = colonneAnnotation.nom();
                            }
                            String setterName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                            Method setterMethod = clazz.getMethod(setterName, field.getType());
                            Object value = rs.getObject(columnName);
                            // Conversion de type (same logic as findAll)
                            if (value instanceof String) {
                                value = value.toString().replace("'", "''");
                            }
                            setterMethod.invoke(instance, value);
                        }
                    }
                    results.add(instance);
                }
            }
        }
        return results;
    }

    /**
    * On entre une connexion et un objet qui définira les critères WHERE et on obtient les données
    * sous-forme de liste d'objets  avec de la pagination
    * 
     * @param co La connexion au SGBD
     * @param o L'objet à mapper
     * @param pageNumber Le numéro de page
     * @param pageSize Le nombre d'éléments par page
     * * * @example
     * ```java
     * // Import nécessaire
    * import java.sql.Connection;
    * import java.util.ArrayList;
    * import java.util.List;

    // Hypothetical object class with @Table annotation (replace with yours)
    @Table(nom = "produits")
    public class Produit {
        private int id;
        private String nom;
        private double prix;
        // Getters et Setters (non affichés pour la concision)
    }

    // Exemple d'utilisation pour la page 2 avec 10 éléments par page
    Connection connection = // Votre connexion à la base de données
    Produit produit = new Produit();
    * produit.setNom("Biscuit");
    int pageNumber = 2;
    int pageSize = 10;

    List<Produit> produits = findByCriteria(connection, produit, pageNumber, pageSize);
    for (Produit p : produits) {
        System.out.println(p.getId() + " - " + p.getNom() + " - " + p.getPrix());
    }
     * ```
    * @return Liste d'objets d'un "SELECT *" d'une SGBD avec un WHERE
    */
    public <T> List<T> findByCriteria(Connection co, T o, int pageNumber, int pageSize) throws Exception {
        Class<?> clazz = o.getClass();
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        String tableName = tableAnnotation != null ? tableAnnotation.nom() : clazz.getSimpleName().toLowerCase();
        // Build WHERE clause dynamically based on non-null fields
        StringBuilder whereClause = new StringBuilder();
        List<Object> valuesList = new ArrayList<>();
        int counter = 0;
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                Method getterMethod = clazz.getMethod(getterName);
                Object value = getterMethod.invoke(o);
                if (value != null) {
                    if (counter > 0) {
                        whereClause.append(" OR ");
                    } else {
                        whereClause.append(" WHERE ");
                    }
                    String columnName = field.getName();
                    Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                    if (colonneAnnotation != null) {
                        columnName = colonneAnnotation.nom();
                    }
                    whereClause.append(columnName + " = ?");
                    valuesList.add(value);
                    counter++;
                }
            }
        }
        // Calculate offset based on page number and page size
        int offset = (pageNumber - 1) * pageSize;
        // Construct paginated SQL query
        String query = "SELECT * FROM " + tableName + " " + whereClause.toString() + " LIMIT " + pageSize + " OFFSET " + offset + ";";
        List<T> results;
        // Set values for prepared statement
        try (PreparedStatement stmt = co.prepareStatement(query)) {
            // Set values for prepared statement
            for (int i = 0; i < valuesList.size(); i++) {
                stmt.setObject(i + 1, valuesList.get(i));
            }  try (ResultSet rs = stmt.executeQuery()) {
                results = new ArrayList<>();
                while (rs.next()) {
                    T instance = (T) clazz.newInstance();
                    for (Field field : clazz.getDeclaredFields()) {
                        if (!Modifier.isStatic(field.getModifiers())) {
                            String columnName = field.getName();
                            Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                            if (colonneAnnotation != null) {
                                columnName = colonneAnnotation.nom();
                            }
                            String setterName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                            Method setterMethod = clazz.getMethod(setterName, field.getType());
                            Object value = rs.getObject(columnName);
                            // Conversion de type (same logic as findAll)
                            if (value instanceof String) {
                                value = value.toString().replace("'", "''");
                            }
                            setterMethod.invoke(instance, value);
                        }
                    }
                    results.add(instance);
                }
            }
        }
        return results;
    }

    /**
    * On entre une connexion et 2 objets qui définiront 2 intervalles et on obtient les données
    * sous-forme de liste d'objets 
    * 
     * @param co La connexion au SGBD
     * @param obj1 L'objet 1 contenant le premier intervalle
     * @param obj2 L'objet 2 contenant le second intervalle
     * @example
    * ```java
    * // Import nécessaire
    * import java.sql.Connection;
    * import java.sql.Date;
    * import java.util.ArrayList;
    * import java.util.List;

    // Hypothetical object class with @Table and @Colonne annotations (replace with yours)
    @Table(nom = "commandes")
    public class Commande {
        private int id;
        @Colonne(nom = "date_commande")
        private Date dateCommande;
        private double montant;
        // Getters et Setters (non affichés pour la concision)
    }

    // Exemple d'utilisation pour rechercher des commandes entre le 01/01/2023 et le 30/06/2023
    Connection connection = // Votre connexion à la base de données
    Commande commande1 = new Commande();
    commande1.setDateCommande(Date.valueOf("2023-01-01"));
    Commande commande2 = new Commande();
    commande2.setDateCommande(Date.valueOf("2023-06-30"));

    List<Commande> commandes = findByInterval(connection, commande1, commande2);
    for (Commande c : commandes) {
        System.out.println(c.getId() + " - " + c.getDateCommande() + " - " + c.getMontant());
    }
    * ```
    * @return Liste d'objets d'un "SELECT *" d'une SGBD avec un "BETWEEN"
    */
    public <T> List<T> findByInterval(Connection co, T obj1, T obj2) throws Exception {
        Class<?> clazz = obj1.getClass();
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        String tableName = tableAnnotation != null ? tableAnnotation.nom() : clazz.getSimpleName().toLowerCase();
        // Get non-null field with `Colonne` annotation
        String fieldName = null;
        Object lowerBound = null;
        Object upperBound = null;
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                if (colonneAnnotation != null) {
                    String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                    Method getterMethod = clazz.getMethod(getterName);
                    Object value1 = getterMethod.invoke(obj1);
                    Object value2 = getterMethod.invoke(obj2);
                    if (value1 != null) {
                        fieldName = colonneAnnotation.nom();
                        lowerBound = value1;
                    }
                    if (value2 != null) {
                        upperBound = value2;
                    }
                    if(value1.equals(value2)){
                        continue;
                    }
                    break;
                }
            }
        }
        // Build query only if at least one bound and field name are non-null
        String query = null;
        if (lowerBound != null || upperBound != null) {
            query = "SELECT * FROM " + tableName + " WHERE " + fieldName + " BETWEEN ? AND ?;";
        }
        if (query != null) {
            PreparedStatement stmt = co.prepareStatement(query);
            if (lowerBound != null) {
                stmt.setObject(1, lowerBound);
            } else {
                stmt.setNull(1, Types.NULL);
            }
            
            if (upperBound != null) {
                stmt.setObject(2, upperBound);
            } else {
                stmt.setNull(2, Types.NULL);
            }
            ResultSet rs = stmt.executeQuery();
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                T instance = (T) clazz.newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        String columnName = field.getName();
                        Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                        if (colonneAnnotation != null) {
                            columnName = colonneAnnotation.nom();
                        }
                        String setterName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                        Method setterMethod = clazz.getMethod(setterName, field.getType());
                        Object value = rs.getObject(columnName);
                        // Conversion de type (même logique que findAll)
                        if (value instanceof String) {
                            value = value.toString().replace("'", "''");
                        }
                        
                        if (value instanceof java.util.Date) {
                            // Handle date fields by converting them to java.sql.Date
                            value = rs.getDate(columnName);
                        }
                        setterMethod.invoke(instance, value);
                    }
                }
                results.add(instance);
            }
            rs.close();
            stmt.close();
            return results;
        } else {
            return new ArrayList<>();
        }
    }

    /**
    * On entre une connexion et 2 objets qui définiront 2 intervalles et on obtient les données
    * sous-forme de liste d'objets avec pagination
    * 
     * @param co La connexion au SGBD
     * @param obj1 L'objet 1 contenant le premier intervalle
     * @param obj2 L'objet 2 contenant le second intervalle
     * @param pageNumber Le numéro de page
     * @param pageSize Le nombre d'éléments par page
     * @example
    * ```java
    * // Import nécessaire
    * import java.sql.Connection;
    * import java.sql.Date;
    * import java.util.ArrayList;
    * import java.util.List;

    // Hypothetical object class with @Table and @Colonne annotations (replace with yours)
    @Table(nom = "factures")
    public class Facture {
        private int id;
        @Colonne(nom = "date_facture")
        private Date dateFacture;
        private double montant;
        // Getters et Setters (non affichés pour la concision)
    }

    // Exemple d'utilisation pour rechercher des factures entre le 01/04/2024 et le 30/04/2024 (page 2 avec 10 éléments par page)
    Connection connection = // Votre connexion à la base de données
    Facture facture1 = new Facture();
    facture1.setDateFacture(Date.valueOf("2024-04-01"));
    Facture facture2 = new Facture();
    facture2.setDateFacture(Date.valueOf("2024-04-30"));
    int pageNumber = 2;
    int pageSize = 10;

    List<Facture> factures = findByInterval(connection, facture1, facture2, pageNumber, pageSize);
    for (Facture f : factures) {
        System.out.println(f.getId() + " - " + f.getDateFacture() + " - " + f.getMontant());
    }
    * ```
    * @return Liste d'objets d'un "SELECT *" d'une SGBD avec un "BETWEEN"
    */
    public <T> List<T> findByInterval(Connection co, T obj1, T obj2, int pageNumber, int pageSize) throws Exception {
        Class<?> clazz = obj1.getClass();
        Table tableAnnotation = clazz.getAnnotation(Table.class);
        String tableName = tableAnnotation != null ? tableAnnotation.nom() : clazz.getSimpleName().toLowerCase();
        // Get non-null field with `Colonne` annotation
        String fieldName = null;
        Object lowerBound = null;
        Object upperBound = null;
        for (Field field : clazz.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) {
                Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                if (colonneAnnotation != null) {
                    String getterName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                    Method getterMethod = clazz.getMethod(getterName);
                    Object value1 = getterMethod.invoke(obj1);
                    Object value2 = getterMethod.invoke(obj2);
                    if (value1 != null) {
                        fieldName = colonneAnnotation.nom();
                        lowerBound = value1;
                    }
                    if (value2 != null) {
                        upperBound = value2;
                    }
                    if(value1.equals(value2)){
                        continue;
                    }
                    break;
                }
            }
        }
        // Build query only if at least one bound and field name are non-null
        String query = null;
        if (lowerBound != null || upperBound != null) {
            // Calculate offset based on page number and page size
            int offset = (pageNumber - 1) * pageSize;
            // Construct paginated SQL query
            query = "SELECT * FROM " + tableName + " WHERE " + fieldName + " BETWEEN ? AND ? LIMIT "+ pageSize + " OFFSET " + offset + ";";
        }
        if (query != null) {
            PreparedStatement stmt = co.prepareStatement(query);
            if (lowerBound != null) {
                stmt.setObject(1, lowerBound);
            } else {
                stmt.setNull(1, Types.NULL);
            }
            
            if (upperBound != null) {
                stmt.setObject(2, upperBound);
            } else {
                stmt.setNull(2, Types.NULL);
            }
            ResultSet rs = stmt.executeQuery();
            List<T> results = new ArrayList<>();
            while (rs.next()) {
                T instance = (T) clazz.newInstance();
                for (Field field : clazz.getDeclaredFields()) {
                    if (!Modifier.isStatic(field.getModifiers())) {
                        String columnName = field.getName();
                        Colonne colonneAnnotation = field.getAnnotation(Colonne.class);
                        if (colonneAnnotation != null) {
                            columnName = colonneAnnotation.nom();
                        }
                        String setterName = "set" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                        Method setterMethod = clazz.getMethod(setterName, field.getType());
                        Object value = rs.getObject(columnName);
                        // Conversion de type (même logique que findAll)
                        if (value instanceof String) {
                            value = value.toString().replace("'", "''");
                        }
                        
                        if (value instanceof java.util.Date) {
                            // Handle date fields by converting them to java.sql.Date
                            value = rs.getDate(columnName);
                        }
                        setterMethod.invoke(instance, value);
                    }
                }
                results.add(instance);
            }
            rs.close();
            stmt.close();
            return results;
        } else {
            return new ArrayList<>();
        }
    }
}
