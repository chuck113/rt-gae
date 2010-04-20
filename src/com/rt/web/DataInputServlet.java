package com.rt.web;

import com.rt.data.DataLoader;
import com.rt.data.RhymeDao;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DataInputServlet extends HttpServlet{

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String albumFile = req.getParameter("albumFile");
        if(albumFile == null || albumFile.length() == 0)return;
        new DataLoader().loadAlbumFile(new RhymeDao(), albumFile);
    }
}
