<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Telosys Web</title>
    <meta name="description" content="Telosys Web">
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link rel="stylesheet" href="lib/font-awesome/css/font-awesome.min.css">
    <link rel="stylesheet" href="lib/bootstrap/3.3.7/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="css/index.css">
    <link href="lib/materialdesignicons/css/materialdesignicons.css" media="all" rel="stylesheet" type="text/css">
</head>
<body>

<header>
    <div class="container">
        <div class="row">
            <div class="col-sm-1">
                <div class="item logo">
                </div>
            </div>
            <div class="col-sm-4">
                <span class="item title">
                    Telosys Saas
                </span>
            </div>
            <div class="col-sm-7 right-align">
                <form class="form-inline" name="loginform" action="login.jsp" method="POST" accept-charset="UTF-8" role="form">
                    <div class="container-fluid">
                        <div class="row">
                            <div class="form-group">
                                <input name="username" id="username" type="text" class="form-control" placeholder="Username" />
                            </div>
                            <div class="form-group">
                                <input name="password" id="password" type="password" class="form-control" placeholder="Password" />
                            </div>
                            <div class="form-group">
                                <button type="submit" class="btn btn-default item">Sign in</button>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</header>

<div class="shade-gradient">
    <div class="login without-backdrop">
        <div class="login-body">
            <form name="createAccountForm" action="createAccount" method="POST">
                <div class="form-group">
                    <a href="/profile/github" class="btn btn-default btn-lg btn-github btn-block" role="button"><i class="fa fa-github fa-2x"></i>Sign in with GitHub</a>
                </div>
                <hr/>
                <% if (request.getSession().getAttribute("error") != null) { %>
                <div class="form-group">
                    <div class="alert alert-danger">
                        <% out.println(request.getSession().getAttribute("error")); %>
                    </div>
                </div>
                <% } %>
                <div class="form-group">
                    <input name="login" id="login" type="text" class="form-control input-lg" placeholder="Username" />
                </div>
                <div class="form-group">
                    <input name="mail" id="mail" type="text" class="form-control input-lg" placeholder="Email Address" />
                </div>
                <div class="form-group">
                    <input name="password1" id="password1" type="password" class="form-control input-lg" placeholder="Password" />
                </div>
                <div class="form-group">
                    <input name="password2" id="password2" type="password" class="form-control input-lg" placeholder="Confirm Password" />
                </div>
                <button type="submit" class="btn btn-success btn-lg btn-block" role="button" data-reactid="86">Create an account</button>
            </form>
        </div>
    </div>
</div>

</body>
</html>
