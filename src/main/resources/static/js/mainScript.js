$.getJSON("user", function (data) {
    let isUserAdmin = false;
    $("#user-name-span").append(data.name);

    for (let i = 0; i < data.roles.length; i++) {
        if (data.roles[i].role === "ROLE_ADMIN") {
            $("#admin-button-div").removeAttr("hidden");
            isUserAdmin = true;
        }
        $("#user-roles-span").append(data.roles[i].role.replace("ROLE_", " "));
    }

    if (!isUserAdmin) {
        $("#user-button-div").click();
    } else {
        $("#table-div").load("admin/page #admin-page-div");
        parseAllUsers();
    }
});

$(document).ready(function () {
    $("#user-button-div").click(function pressUserButton() {
        if ($("#user-button-div").is('.active')) {
            $("#table-div").load("user/page #user-page-div");
            $.getJSON("user", function (data) {
                parseCurrJsonUser(data)
            });
            $(this).toggleClass('text-white text-primary bg-primary active');
            $("#admin-button-div").toggleClass('text-white text-primary bg-primary');
            if (!$("#admin-button-div").is('.active')) {
                $("#admin-button-div").toggleClass('active')
            }
        }
    }).hover(function () {
        $(this).toggleClass('text-primary text-white bg-dark');
    });
});

$(document).ready(function () {
    $("#admin-button-div").hover(function () {
        $(this).toggleClass('text-primary text-white bg-dark');
    }).click(function () {
        if ($("#admin-button-div").is('.active')) {
            $("#table-div").load("admin/page #admin-page-div");
            parseAllUsers();
            $(this).toggleClass('text-white text-primary bg-primary active');
            $("#user-button-div").toggleClass('text-white text-primary bg-primary');
            if (!$("#user-button-div").is('.active')) {
                $("#user-button-div").toggleClass('active')
            }
        }
    });
    $("#modal-edit-button").click(function () {
        if ($("#modal-edit-button").text().includes("Сохранить")) {
            let object = {};
            object.id = $("#modal-input-id").val();
            object.name = $("#modal-input-name").val();
            object.age = $("#modal-input-age").val();
            object.email = $("#modal-input-email").val();
            object.roles = [];
            if ($("#modal-input-role-user").is(":checked")) {
                object.roles.push("ROLE_USER");
            }
            if ($("#modal-input-role-admin").is(":checked")) {
                object.roles.push("ROLE_ADMIN");
            }
            saveUser(object);
        } else {
            deleteStudent($("#modal-input-id").val());
        }
    });
});

function parseCurrJsonUser(user) {
    let tr = [];
    tr.push("<tr>");
    tr.push("<td>" + user.id + "</td>");
    tr.push("<td>" + user.name + "</td>");
    tr.push("<td>" + user.email + "</td>");
    tr.push("<td>" + user.age + "</td>");
    tr.push("<td>");
    for (let i = 0; i < user.roles.length; i++) {
        tr.push(user.roles[i].role.replace("ROLE_", " "))
    }
    tr.push("</td>");
    tr.push("</tr>");
    $("#user-page-table-tbody").append(tr.join(''));
}

function parseJsonUserForAdmin(user) {
    let tr = [];
    tr.push("<tr class=\"border-top\">");
    tr.push("<td>" + user.id + "</td>");
    tr.push("<td>" + user.name + "</td>");
    tr.push("<td>" + user.email + "</td>");
    tr.push("<td>" + user.age + "</td>");
    tr.push("<td>");
    for (let i = 0; i < user.roles.length; i++) {
        tr.push(user.roles[i].role.replace("ROLE_", " "))
    }
    tr.push("</td>");
    tr.push('<td> <input class=\"btn border-0 text-white bg-warning\" type=\"button" data-bs-toggle="modal" data-bs-target="#edit-modal" onclick="editStudent('
        + user.id
        + ');" value="Изменить"></td>');
    tr.push('<td> <input class=\"btn border-0 text-white bg-danger\" type=\"button" data-bs-toggle="modal" data-bs-target="#edit-modal" onclick="deleteStudentButton('
        + user.id
        + ');" value="Удалить"></td>');
    tr.push("</tr>");
    $("#admin-page-table-tbody").append(tr.join(''));
}

function parseAllUsers() {
    $.getJSON("admin/users", function (jsons) {
        $("#admin-page-table-tbody").empty();
        for (let i = 0; i < jsons.length; i++) {
            parseJsonUserForAdmin(jsons[i]);
        }
    });
}

function editStudent(id) {
    $.getJSON("admin/user/" + id, function (user) {
        $('#modal-h2').text("Форма пользователя");
        $("#modal-input-id").val(user.id);
        $("#modal-input-name").val(user.name).prop('disabled', false);
        $("#modal-input-email").val(user.email).prop('disabled', false);
        $("#modal-input-age").val(user.age).prop('disabled', false);
        $("#modal-input-role").prop('disabled', false);
        $("#modal-edit-button").text("Сохранить").addClass("btn-primary").removeClass("btn-danger");
    });
}

function deleteStudentButton(id) {
    $.getJSON("admin/user/" + id, function (user) {
        $('#modal-h2').text("Delete User");
        $("#modal-input-id").val(user.id);
        $("#modal-input-name").val(user.name).prop('disabled', true);
        $("#modal-input-email").val(user.email).prop('disabled', true);
        $("#modal-input-age").val(user.age).prop('disabled', true);
        $("#modal-input-role").prop('disabled', true);
        $("#modal-edit-button").text("Удалить").removeClass("btn-primary").addClass("btn-danger");
    });
}

function deleteStudent(id) {
    $.ajax({
        url: "admin/user/" + id,
        type: 'DELETE',
        success: function () {
            parseAllUsers();
        }
    });
}

function saveUser(object) {
    $.post("admin/user", object, function () {
        parseAllUsers();
    });
}