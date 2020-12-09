/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.grpc.client.user;

import java.util.List;

import com.sitewhere.grpc.client.ApiChannel;
import com.sitewhere.grpc.client.GrpcUtils;
import com.sitewhere.grpc.client.spi.client.IUserManagementApiChannel;
import com.sitewhere.grpc.service.GAddRolesRequest;
import com.sitewhere.grpc.service.GAddRolesResponse;
import com.sitewhere.grpc.service.GCreateGrantedAuthorityRequest;
import com.sitewhere.grpc.service.GCreateGrantedAuthorityResponse;
import com.sitewhere.grpc.service.GCreateRoleRequest;
import com.sitewhere.grpc.service.GCreateRoleResponse;
import com.sitewhere.grpc.service.GCreateUserRequest;
import com.sitewhere.grpc.service.GCreateUserResponse;
import com.sitewhere.grpc.service.GDeleteGrantedAuthorityRequest;
import com.sitewhere.grpc.service.GDeleteRoleRequest;
import com.sitewhere.grpc.service.GDeleteUserRequest;
import com.sitewhere.grpc.service.GDeleteUserResponse;
import com.sitewhere.grpc.service.GGetAccessTokenRequest;
import com.sitewhere.grpc.service.GGetAccessTokenResponse;
import com.sitewhere.grpc.service.GGetGrantedAuthorityByNameRequest;
import com.sitewhere.grpc.service.GGetGrantedAuthorityByNameResponse;
import com.sitewhere.grpc.service.GGetPublicKeyRequest;
import com.sitewhere.grpc.service.GGetPublicKeyResponse;
import com.sitewhere.grpc.service.GGetRoleByNameRequest;
import com.sitewhere.grpc.service.GGetRoleByNameResponse;
import com.sitewhere.grpc.service.GGetRolesRequest;
import com.sitewhere.grpc.service.GGetRolesResponse;
import com.sitewhere.grpc.service.GGetUserByUsernameRequest;
import com.sitewhere.grpc.service.GGetUserByUsernameResponse;
import com.sitewhere.grpc.service.GListGrantedAuthoritiesRequest;
import com.sitewhere.grpc.service.GListGrantedAuthoritiesResponse;
import com.sitewhere.grpc.service.GListRolesRequest;
import com.sitewhere.grpc.service.GListRolesResponse;
import com.sitewhere.grpc.service.GListUsersRequest;
import com.sitewhere.grpc.service.GListUsersResponse;
import com.sitewhere.grpc.service.GRemoveRolesRequest;
import com.sitewhere.grpc.service.GRemoveRolesResponse;
import com.sitewhere.grpc.service.GUpdateGrantedAuthorityRequest;
import com.sitewhere.grpc.service.GUpdateGrantedAuthorityResponse;
import com.sitewhere.grpc.service.GUpdateRoleRequest;
import com.sitewhere.grpc.service.GUpdateRoleResponse;
import com.sitewhere.grpc.service.GUpdateUserRequest;
import com.sitewhere.grpc.service.GUpdateUserResponse;
import com.sitewhere.grpc.service.UserManagementGrpc;
import com.sitewhere.grpc.user.UserModelConverter;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.microservice.IFunctionIdentifier;
import com.sitewhere.spi.microservice.MicroserviceIdentifier;
import com.sitewhere.spi.microservice.grpc.GrpcServiceIdentifier;
import com.sitewhere.spi.microservice.grpc.IGrpcServiceIdentifier;
import com.sitewhere.spi.microservice.grpc.IGrpcSettings;
import com.sitewhere.spi.microservice.instance.IInstanceSettings;
import com.sitewhere.spi.search.ISearchResults;
import com.sitewhere.spi.user.IGrantedAuthority;
import com.sitewhere.spi.user.IGrantedAuthoritySearchCriteria;
import com.sitewhere.spi.user.IRole;
import com.sitewhere.spi.user.IRoleSearchCriteria;
import com.sitewhere.spi.user.IUser;
import com.sitewhere.spi.user.IUserSearchCriteria;
import com.sitewhere.spi.user.request.IGrantedAuthorityCreateRequest;
import com.sitewhere.spi.user.request.IRoleCreateRequest;
import com.sitewhere.spi.user.request.IUserCreateRequest;

/**
 * Supports SiteWhere user management APIs on top of a
 * {@link UserManagementGrpcChannel}.
 */
public class UserManagementApiChannel extends ApiChannel<UserManagementGrpcChannel>
	implements IUserManagementApiChannel<UserManagementGrpcChannel> {

    public UserManagementApiChannel(IInstanceSettings settings) {
	super(settings, MicroserviceIdentifier.InstanceManagement, GrpcServiceIdentifier.UserManagement,
		IGrpcSettings.USER_MANAGEMENT_API_PORT);
    }

    /*
     * @see
     * com.sitewhere.grpc.client.spi.IApiChannel#createGrpcChannel(com.sitewhere.spi
     * .microservice.instance.IInstanceSettings,
     * com.sitewhere.spi.microservice.IFunctionIdentifier,
     * com.sitewhere.spi.microservice.grpc.IGrpcServiceIdentifier, int)
     */
    @Override
    public UserManagementGrpcChannel createGrpcChannel(IInstanceSettings settings, IFunctionIdentifier identifier,
	    IGrpcServiceIdentifier grpcServiceIdentifier, int port) {
	return new UserManagementGrpcChannel(settings, identifier, grpcServiceIdentifier, port);
    }

    /*
     * @see
     * com.sitewhere.microservice.api.user.IUserManagement#createUser(com.sitewhere.
     * spi.user.request.IUserCreateRequest)
     */
    @Override
    public IUser createUser(IUserCreateRequest request) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getCreateUserMethod());
	    GCreateUserRequest.Builder grequest = GCreateUserRequest.newBuilder();
	    grequest.setRequest(UserModelConverter.asGrpcUserCreateRequest(request));
	    GCreateUserResponse gresponse = getGrpcChannel().getBlockingStub().createUser(grequest.build());
	    IUser response = UserModelConverter.asApiUser(gresponse.getUser());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getCreateUserMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getCreateUserMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.microservice.api.user.IUserManagement#getAccessToken(java.lang.
     * String, java.lang.String)
     */
    @Override
    public String getAccessToken(String username, String password) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getGetAccessTokenMethod());
	    GGetAccessTokenRequest.Builder grequest = GGetAccessTokenRequest.newBuilder();
	    grequest.setUsername(username);
	    grequest.setPassword(password);
	    GGetAccessTokenResponse gresponse = getGrpcChannel().getBlockingStub().getAccessToken(grequest.build());
	    String response = gresponse.getToken();
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getGetAccessTokenMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getGetAccessTokenMethod(), t);
	}
    }

    /*
     * @see com.sitewhere.microservice.api.user.IUserManagement#getPublicKey()
     */
    @Override
    public String getPublicKey() throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getGetPublicKeyMethod());
	    GGetPublicKeyRequest.Builder grequest = GGetPublicKeyRequest.newBuilder();
	    GGetPublicKeyResponse gresponse = getGrpcChannel().getBlockingStub().getPublicKey(grequest.build());
	    String response = gresponse.getKey();
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getGetPublicKeyMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getGetPublicKeyMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#updateUser(java.lang.String,
     * com.sitewhere.spi.user.request.IUserCreateRequest, boolean)
     */
    @Override
    public IUser updateUser(String username, IUserCreateRequest request, boolean encodePassword)
	    throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getUpdateUserMethod());
	    GUpdateUserRequest.Builder grequest = GUpdateUserRequest.newBuilder();
	    grequest.setUsername(username);
	    grequest.setRequest(UserModelConverter.asGrpcUserCreateRequest(request));
	    grequest.setEncodePassword(encodePassword);
	    GUpdateUserResponse gresponse = getGrpcChannel().getBlockingStub().updateUser(grequest.build());
	    IUser response = UserModelConverter.asApiUser(gresponse.getUser());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getUpdateUserMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getUpdateUserMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#getUserByUsername(java.lang.
     * String)
     */
    @Override
    public IUser getUserByUsername(String username) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getGetUserByUsernameMethod());
	    GGetUserByUsernameRequest.Builder grequest = GGetUserByUsernameRequest.newBuilder();
	    grequest.setUsername(username);
	    GGetUserByUsernameResponse gresponse = getGrpcChannel().getBlockingStub()
		    .getUserByUsername(grequest.build());
	    IUser response = (gresponse.hasUser()) ? UserModelConverter.asApiUser(gresponse.getUser()) : null;
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getGetUserByUsernameMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getGetUserByUsernameMethod(), t);
	}
    }

    /*
     * @see com.sitewhere.spi.user.IUserManagement#listUsers(com.sitewhere.spi.user.
     * IUserSearchCriteria)
     */
    @Override
    public ISearchResults<IUser> listUsers(IUserSearchCriteria criteria) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getListUsersMethod());
	    GListUsersRequest.Builder grequest = GListUsersRequest.newBuilder();
	    grequest.setCriteria(UserModelConverter.asGrpcUserSearchCriteria(criteria));
	    GListUsersResponse gresponse = getGrpcChannel().getBlockingStub().listUsers(grequest.build());
	    ISearchResults<IUser> results = UserModelConverter.asApiUserSearchResults(gresponse.getResults());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getListUsersMethod(), results);
	    return results;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getListUsersMethod(), t);
	}
    }

    /*
     * @see com.sitewhere.spi.user.IUserManagement#deleteUser(java.lang.String)
     */
    @Override
    public IUser deleteUser(String username) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getDeleteUserMethod());
	    GDeleteUserRequest.Builder grequest = GDeleteUserRequest.newBuilder();
	    grequest.setUsername(username);
	    GDeleteUserResponse gresponse = getGrpcChannel().getBlockingStub().deleteUser(grequest.build());
	    IUser response = UserModelConverter.asApiUser(gresponse.getUser());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getDeleteUserMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getDeleteUserMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#createGrantedAuthority(com.
     * sitewhere.spi.user.request.IGrantedAuthorityCreateRequest)
     */
    @Override
    public IGrantedAuthority createGrantedAuthority(IGrantedAuthorityCreateRequest request) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getCreateGrantedAuthorityMethod());
	    GCreateGrantedAuthorityRequest.Builder grequest = GCreateGrantedAuthorityRequest.newBuilder();
	    grequest.setRequest(UserModelConverter.asGrpcGrantedAuthorityCreateRequest(request));
	    GCreateGrantedAuthorityResponse gresponse = getGrpcChannel().getBlockingStub()
		    .createGrantedAuthority(grequest.build());
	    IGrantedAuthority response = UserModelConverter.asApiGrantedAuthority(gresponse.getAuthority());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getCreateGrantedAuthorityMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getCreateGrantedAuthorityMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#getGrantedAuthorityByName(java.
     * lang.String)
     */
    @Override
    public IGrantedAuthority getGrantedAuthorityByName(String name) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getGetGrantedAuthorityByNameMethod());
	    GGetGrantedAuthorityByNameRequest.Builder grequest = GGetGrantedAuthorityByNameRequest.newBuilder();
	    grequest.setName(name);
	    GGetGrantedAuthorityByNameResponse gresponse = getGrpcChannel().getBlockingStub()
		    .getGrantedAuthorityByName(grequest.build());
	    if (gresponse.hasAuthority()) {
		IGrantedAuthority response = UserModelConverter.asApiGrantedAuthority(gresponse.getAuthority());
		GrpcUtils.logClientMethodResponse(UserManagementGrpc.getGetGrantedAuthorityByNameMethod(), response);
		return response;
	    }
	    return null;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getGetGrantedAuthorityByNameMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#updateGrantedAuthority(java.lang.
     * String, com.sitewhere.spi.user.request.IGrantedAuthorityCreateRequest)
     */
    @Override
    public IGrantedAuthority updateGrantedAuthority(String name, IGrantedAuthorityCreateRequest request)
	    throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getUpdateGrantedAuthorityMethod());
	    GUpdateGrantedAuthorityRequest.Builder grequest = GUpdateGrantedAuthorityRequest.newBuilder();
	    grequest.setName(name);
	    grequest.setRequest(UserModelConverter.asGrpcGrantedAuthorityCreateRequest(request));
	    GUpdateGrantedAuthorityResponse gresponse = getGrpcChannel().getBlockingStub()
		    .updateGrantedAuthority(grequest.build());
	    IGrantedAuthority response = UserModelConverter.asApiGrantedAuthority(gresponse.getAuthority());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getUpdateGrantedAuthorityMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getUpdateGrantedAuthorityMethod(), t);
	}
    }

    /*
     * @see
     * com.sitewhere.spi.user.IUserManagement#listGrantedAuthorities(com.sitewhere.
     * spi.user.IGrantedAuthoritySearchCriteria)
     */
    @Override
    public ISearchResults<IGrantedAuthority> listGrantedAuthorities(IGrantedAuthoritySearchCriteria criteria)
	    throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getListGrantedAuthoritiesMethod());
	    GListGrantedAuthoritiesRequest.Builder grequest = GListGrantedAuthoritiesRequest.newBuilder();
	    GListGrantedAuthoritiesResponse gresponse = getGrpcChannel().getBlockingStub()
		    .listGrantedAuthorities(grequest.build());
	    ISearchResults<IGrantedAuthority> results = UserModelConverter
		    .asApiGrantedAuthoritySearchResults(gresponse.getResults());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getListGrantedAuthoritiesMethod(), results);
	    return results;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getListGrantedAuthoritiesMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#deleteGrantedAuthority(java.lang.
     * String)
     */
    @Override
    public void deleteGrantedAuthority(String authority) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getDeleteGrantedAuthorityMethod());
	    GDeleteGrantedAuthorityRequest.Builder grequest = GDeleteGrantedAuthorityRequest.newBuilder();
	    grequest.setName(authority);
	    getGrpcChannel().getBlockingStub().deleteGrantedAuthority(grequest.build());
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getDeleteGrantedAuthorityMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#getRoles(java.lang.String)
     */
    @Override
    public List<IRole> getRoles(String username) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getGetRolesForUserMethod());
	    GGetRolesRequest.Builder grequest = GGetRolesRequest.newBuilder();
	    grequest.setUsername(username);
	    GGetRolesResponse gresponse = getGrpcChannel().getBlockingStub().getRolesForUser(grequest.build());

	    List<IRole> response = UserModelConverter.asApiRoles(gresponse.getRolesList());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getGetRolesForUserMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getGetRolesForUserMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#addRoles(java.lang.String,
     * java.util.List)
     */
    @Override
    public List<IRole> addRoles(String username, List<String> roles) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getAddRolesForUserMethod());
	    GAddRolesRequest.Builder grequest = GAddRolesRequest.newBuilder();
	    grequest.setUsername(username);
	    grequest.getRolesList().addAll(roles);

	    GAddRolesResponse gresponse = getGrpcChannel().getBlockingStub().addRolesForUser(grequest.build());
	    List<IRole> response = UserModelConverter.asApiRoles(gresponse.getRolesList());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getAddRolesForUserMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getAddRolesForUserMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#removeRoles(java.lang.String,
     * java.util.List)
     */
    @Override
    public List<IRole> removeRoles(String username, List<String> roles) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getRemoveRolesForUserMethod());
	    GRemoveRolesRequest.Builder grequest = GRemoveRolesRequest.newBuilder();
	    grequest.setUsername(username);
	    grequest.getRolesList().addAll(roles);
	    GRemoveRolesResponse gresponse = getGrpcChannel().getBlockingStub().removeRolesForUser(grequest.build());
	    List<IRole> response = UserModelConverter.asApiRoles(gresponse.getRolesList());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getRemoveRolesForUserMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getRemoveRolesForUserMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.sitewhere.spi.user.IUserManagement#createRole(com.sitewhere.spi.user.
     * request.IRoleCreateRequest)
     */
    @Override
    public IRole createRole(IRoleCreateRequest request) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getCreateRoleMethod());
	    GCreateRoleRequest.Builder grequest = GCreateRoleRequest.newBuilder();
	    grequest.setRequest(UserModelConverter.asGrpcRoleCreateRequest(request));
	    GCreateRoleResponse gresponse = getGrpcChannel().getBlockingStub().createRole(grequest.build());
	    IRole response = UserModelConverter.asApiRole(gresponse.getRole());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getCreateGrantedAuthorityMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getCreateGrantedAuthorityMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#getRoleByName(java.lang.String)
     */
    @Override
    public IRole getRoleByName(String name) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getGetRoleByNameMethod());
	    GGetRoleByNameRequest.Builder grequest = GGetRoleByNameRequest.newBuilder();
	    grequest.setName(name);
	    GGetRoleByNameResponse gresponse = getGrpcChannel().getBlockingStub().getRoleByName(grequest.build());
	    if (gresponse.hasRole()) {
		IRole response = UserModelConverter.asApiRole(gresponse.getRole());
		GrpcUtils.logClientMethodResponse(UserManagementGrpc.getGetGrantedAuthorityByNameMethod(), response);
		return response;
	    }
	    return null;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getGetGrantedAuthorityByNameMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#updateRole(java.lang.String,
     * com.sitewhere.spi.user.request.IRoleCreateRequest)
     */
    @Override
    public IRole updateRole(String name, IRoleCreateRequest request) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getUpdateRoleMethod());
	    GUpdateRoleRequest.Builder grequest = GUpdateRoleRequest.newBuilder();
	    grequest.setName(name);
	    grequest.setRequest(UserModelConverter.asGrpcRoleCreateRequest(request));
	    GUpdateRoleResponse gresponse = getGrpcChannel().getBlockingStub().updateRole(grequest.build());
	    IRole response = UserModelConverter.asApiRole(gresponse.getAuthority());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getUpdateGrantedAuthorityMethod(), response);
	    return response;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getUpdateGrantedAuthorityMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#listRoles(com.sitewhere.spi.user.
     * IRoleSearchCriteria)
     */
    @Override
    public ISearchResults<IRole> listRoles(IRoleSearchCriteria criteria) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getListRolesMethod());
	    GListRolesRequest.Builder grequest = GListRolesRequest.newBuilder();
	    GListRolesResponse gresponse = getGrpcChannel().getBlockingStub().listRoles(grequest.build());
	    ISearchResults<IRole> results = UserModelConverter.asApiRoleSearchResults(gresponse.getResults());
	    GrpcUtils.logClientMethodResponse(UserManagementGrpc.getListRolesMethod(), results);
	    return results;
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getListGrantedAuthoritiesMethod(), t);
	}
    }

    /*
     * (non-Javadoc)
     *
     * @see com.sitewhere.spi.user.IUserManagement#deleteRole(java.lang.String)
     */
    @Override
    public void deleteRole(String role) throws SiteWhereException {
	try {
	    GrpcUtils.handleClientMethodEntry(this, UserManagementGrpc.getDeleteRoleMethod());
	    GDeleteRoleRequest.Builder grequest = GDeleteRoleRequest.newBuilder();
	    grequest.setName(role);
	    getGrpcChannel().getBlockingStub().deleteRole(grequest.build());
	} catch (Throwable t) {
	    throw GrpcUtils.handleClientMethodException(UserManagementGrpc.getDeleteGrantedAuthorityMethod(), t);
	}
    }

}