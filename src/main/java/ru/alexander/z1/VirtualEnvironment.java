package ru.alexander.z1;

import ru.alexander.z1.commands.*;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class VirtualEnvironment {
    private final static SimpleDateFormat df = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private final static String[][] osStartupRules = {
            { "/os", "mkdir /os", "access /os os" },
            { "/os/meta", "mkdir /os/meta", "access /os/meta os" }
    };
    private final static Map<String, Command> commands = new HashMap<>();
    static {
        commands.put("exit", new Exit());
        commands.put("run", new Run());

        commands.put("cat", new Cat());
        commands.put("echo", new Echo());
        commands.put("ls", new Ls());

        commands.put("sudo", new Sudo());
        commands.put("cd", new Cd());
        commands.put("access", new Access());

        commands.put("mkdir", new Mkdir());
        commands.put("create", new Create());
        commands.put("delete", new Delete());
        commands.put("write", new Write());
    }

    private final Map<String, String> par;
    private final URI uri;


    private final Map<String, AccessLevel> fileAccessLevels = new HashMap<>();


    private FileSystem fs;
    public AccessLevel level;
    public String path = "/";

    public VirtualEnvironment(String zipPath) throws IOException {
        File file = new File(zipPath);
        if (!file.getName().endsWith(".zip"))
            throw new IOException("Not a zip file!");

        // Bootloader running
        level = AccessLevel.BOOTLOADER;
        par = new HashMap<>();
        par.put("create", "true");

        Path path = Paths.get(zipPath);
        uri = URI.create("jar:" + path.toUri());
        fs = FileSystems.newFileSystem(uri, par);

        for (int i = 0; i < osStartupRules.length; i++) {
            String[] rule = osStartupRules[i];
            if (Files.notExists(fs.getPath(rule[0]))) {
                System.err.println("Error! File " + rule[0] + " not found! Recovery...");
                System.err.flush();
                for (int j = 1; j < rule.length; j++)
                    System.out.print(executeCommand(rule[j]));
            }
        }



        // OS Loading
        level = AccessLevel.OS;
        if (Files.exists(fs.getPath("/os/meta/access.meta"))) {
            InputStream fis = Files.newInputStream(fs.getPath("/os/meta/access.meta"));
            String[] lines = new String(fis.readAllBytes()).replace("\r", "").split("\n");
            fis.close();
            for (int i = 0; i < lines.length; i++) {
                String[] data = lines[i].split(" ");
                fileAccessLevels.put(data[0], AccessLevel.valueOf(data[1]));
            }
        }
        else {
            Path accessMetaFile = Files.createFile(fs.getPath("/os/meta/access.meta"));
            fileAccessLevels.put(accessMetaFile.toString(), AccessLevel.OS);
        }
        updateAccessMeta();


        // Go to user mode
        level = AccessLevel.USER;
    }

    private void updateAccessMeta() throws IOException {
        PrintWriter writer = new PrintWriter(Files.newOutputStream(fs.getPath("/os/meta/access.meta")));
        fileAccessLevels.forEach((path, level) -> writer.println(path + " " + level.name()));
        writer.close();
    }

    public String executeCommand(String command) {
        int pos = command.indexOf(" ");
        String args;
        if (pos == -1) {
            pos = command.length();
            args = "";
        } else args = command.substring(pos + 1);
        String cmdStr = command.substring(0, pos);
        Command cmd = commands.get(cmdStr);
        if (cmd == null) {
            if (cmdStr.isEmpty()) return "";
            return "Command " + cmdStr + " not found!\n";
        }

        String str = cmd.execute(this, cmdStr, args);

        if (fs == null) return str;
        try {
            fs.close();
            fs = FileSystems.newFileSystem(uri, par);
            return str;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String recoverCommand(String command, String recoverData) throws IOException {
        int pos = command.indexOf(" ");
        String args;
        if (pos == -1) {
            pos = command.length();
            args = "";
        }
        else args = command.substring(pos + 1);
        String cmdStr = command.substring(0, pos);
        Command cmd = commands.get(cmdStr);
        if (cmd == null) {
            if (cmdStr.isEmpty()) return "";
            return "Command " + cmdStr + " not found!\n";
        }

        String str = cmd.recover(this, cmdStr, args, recoverData);

        if (fs == null) return str;
        fs.close();
        fs = FileSystems.newFileSystem(uri, par);
        return str;
    }



    public InputStream getInputStream(String path) throws IOException {
        Path lpath = getPath(path);
        if (lpath == null) throw new IOException("Wrong format!");

        return Files.newInputStream(lpath);
    }
    public OutputStream getOutputStream(String path) throws IOException {
        Path lpath = getPath(path);
        if (lpath == null) throw new IOException("Wrong format!");

        AccessLevel accessLevel = fileAccessLevels.getOrDefault(lpath.toString(), AccessLevel.USER);
        if (accessLevel.getSecurityLevel() > level.getSecurityLevel())
            throw new AccessDeniedException("Try to edit protected file! Need access level: " + accessLevel.name());

        return Files.newOutputStream(lpath);
    }

    public void createDir(String path, boolean updateMeta) throws IOException {
        Path lpath = getPath(path);
        if (lpath == null) throw new IOException("Wrong format!");

        Files.createDirectories(lpath);

        fileAccessLevels.put(lpath.toString(), AccessLevel.USER);
        if (updateMeta) updateAccessMeta();
    }
    public boolean existsInFS(String path) {
        Path lpath = getPath(path);
        if (lpath == null) return false;
        return Files.exists(lpath);
    }
    public void createFile(String path, boolean updateMeta) throws IOException {
        Path lpath = getPath(path);
        if (lpath == null) throw new IOException("Wrong format!");

        Files.createFile(lpath);

        fileAccessLevels.put(lpath.toString(), AccessLevel.USER);
        if (updateMeta) updateAccessMeta();
    }
    public void deleteFromFS(String path, boolean updateMeta) throws IOException {
        Path lpath = getPath(path);
        if (lpath == null) throw new IOException("Wrong format!");

        AccessLevel accessLevel = fileAccessLevels.getOrDefault(lpath.toString(), AccessLevel.USER);
        if (accessLevel.getSecurityLevel() > level.getSecurityLevel())
            throw new AccessDeniedException("Try to delete protected file! Need access level: " + accessLevel.name());


        Files.delete(lpath);

        fileAccessLevels.remove(lpath.toString());
        if (updateMeta) updateAccessMeta();
    }
    public List<Path> list(String path) throws IOException {
        Path lpath = getPath(path);
        if (lpath == null) throw new IOException("Wrong format!");

        DirectoryStream<Path> stream = Files.newDirectoryStream(lpath);
        List<Path> childs = new ArrayList<>();
        for (Path p : stream)
            childs.add(p);
        return childs;
    }
    public void changeAccessLevel(String path, AccessLevel level, boolean updateMeta) throws IOException {
        Path lpath = getPath(path);
        if (lpath == null) throw new IOException("Wrong format!");

        AccessLevel accessLevel = fileAccessLevels.getOrDefault(lpath.toString(), AccessLevel.USER);
        if (accessLevel.getSecurityLevel() > this.level.getSecurityLevel())
            throw new AccessDeniedException("Try to edit protected file! Need access level: " + accessLevel.name());
        if (level.getSecurityLevel() > this.level.getSecurityLevel())
            throw new AccessDeniedException("Try to set higher level than you! Need access level: " + level.name());

        fileAccessLevels.put(lpath.toString(), level);
        if (updateMeta) updateAccessMeta();
    }

    private Path getPath(String path) {
        Path lpath;
        String[] split = path.split("/");
        if (path.startsWith("/")) {
            if (split.length == 0) return null;
            for (int i = 1; i < split.length; i++)
                if (split[i].replace(".", "").isEmpty())
                    return null;
            lpath = fs.getPath(path);
        }
        else {
            if (path.isEmpty()) return fs.getPath(this.path);
            if (split.length == 0) return null;
            for (int i = 0; i < split.length; i++)
                if (split[i].replace(".", "").isEmpty())
                    return null;
            lpath = fs.getPath(this.path + path);
        }
        return lpath;
    }

    public AccessLevel getFileAccessLevel(String path) {
        return fileAccessLevels.getOrDefault(path, AccessLevel.USER);
    }
    public void close() throws IOException {
        if (fs != null) {
            updateAccessMeta();
            fs.close();
            fs = null;
        }
    }

    public boolean isClosing() {
        return fs == null;
    }
}
